pipeline {
  agent any

  tools {
    jdk 'jdk17'          // Настрой в Jenkins: Manage Jenkins → Tools → JDK installations
  }

  environment {
    GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    // Для Docker stage (если используешь): имя образа и реестра
    IMAGE = "my-registry.example.com/familyshop/backend"
    // Название SonarQube-сервера в Jenkins (Manage Jenkins → System → SonarQube servers)
    SONARQUBE_SERVER = 'sonarqube-server'
  }

  stages {
    stage('Checkout') {
      steps {
      checkout scm
      script {
        // 1) macOS автодетект JDK 21
        def detected = sh(script: '(/usr/libexec/java_home -v 21) 2>/dev/null || true', returnStdout: true).trim()
        def jdkHome = detected

        // 2) Фолбэк: Jenkins Tool "jdk21" (должен быть настроен в Global Tool Configuration)
        if (!jdkHome) {
          try {
            jdkHome = tool name: 'jdk21', type: 'jdk'
          } catch (ignored) { /* не настроен */ }
        }

        // 3) Если всё ещё пусто — явно ошибку с подсказкой
        if (!jdkHome?.trim()) {
          error """
              No JDK 21 found.
              - На macOS установи Temurin 21:   brew install --cask temurin21
              - Или в Jenkins: Manage Jenkins → Tools → JDK installations → добавь JDK с именем 'jdk21'
              - Либо используй Docker-агент (см. вариант 2 ниже)
          """
        }

        env.JAVA_HOME = jdkHome
        env.ORG_GRADLE_JAVA_HOME = jdkHome
        env.PATH = "${jdkHome}/bin:${env.PATH}"
      }
      sh 'echo "JAVA_HOME=$JAVA_HOME" && java -version'
      sh 'chmod +x ./gradlew || true'
      }
    }

    stage('Build') {
      steps {
        sh "./gradlew --no-daemon clean assemble -x test"
      }
    }

    stage('Tests') {
      steps {
        // Юнит + интеграционные (Testcontainers)
        sh "./gradlew --no-daemon test jacocoTestReport"
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
          // Если используешь плагин JaCoCo в Jenkins — можно опубликовать:
          // jacoco execPattern: 'build/jacoco/test.exec', classPattern: 'build/classes/java/main', sourcePattern: 'src/main/java'
          archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          // Передаём URL/Token в Gradle через -D, чтобы плагин их увидел
          sh """
             ./gradlew --no-daemon sonarqube \
               -Dsonar.host.url=$SONAR_HOST_URL \
               -Dsonar.login=$SONAR_AUTH_TOKEN
          """
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 15, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        sh "./gradlew --no-daemon clean build"
        archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: true
      }
    }

    stage('Docker Build & Push') {
      when { allOf { branch 'main'; expression { return fileExists('Dockerfile') } } }
      steps {
        script {
          def tag = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          sh """
            docker build -t ${IMAGE}:${tag} -t ${IMAGE}:latest .
            # docker login... (если реестр требует логин)
            # docker push ${IMAGE}:${tag}
            # docker push ${IMAGE}:latest
          """
        }
      }
    }
  }

  post {
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}