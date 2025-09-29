pipeline {
  agent any

  tools {
    jdk 'jdk21'          // Настрой в Jenkins: Manage Jenkins → Tools → JDK installations
  }

  environment {
    GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    // Название SonarQube-сервера в Jenkins (Manage Jenkins → System → SonarQube servers)
    SONARQUBE_SERVER = 'sonarqube-server'
    GITHUB_CREDENTIALS = '08fbc2eb-0bd3-4593-89e7-a73d887e8ab9'   // <-- ID твоего GitHub PAT
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'chmod +x ./gradlew || true'
        script {
          // ставим общий статус PENDING
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci', status: 'PENDING',
                       description: 'Pipeline started', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
      }
    }

    stage('Build') {
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/build', status: 'PENDING',
                       description: 'Build running', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        sh "./gradlew --no-daemon clean assemble -x test"
      }
      post {
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/build', status: 'SUCCESS',
                       description: 'Build passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        failure {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/build', status: 'FAILURE',
                       description: 'Build failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
      }
    }

    stage('Tests') {
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/tests', status: 'PENDING',
                       description: 'Tests running', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        sh "./gradlew --no-daemon test jacocoTestReport"
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
          archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
        }
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/tests', status: 'SUCCESS',
                       description: 'Tests passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        failure {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/tests', status: 'FAILURE',
                       description: 'Tests failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
      }
    }


stage('SonarQube Analysis') {
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/sonarqube', status: 'PENDING',
                       description: 'SonarQube analysis running', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
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
          script {
            def qg = waitForQualityGate() // abortPipeline: true по умолчанию в новых версиях не обязателен
            if (qg.status != 'OK') {
              githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/sonarqube', status: 'FAILURE',
                           description: "Quality Gate: ${qg.status}", targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
              error "Quality Gate: ${qg.status}"
            }
          }
        }
      }
      post {
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/sonarqube', status: 'SUCCESS',
                       description: 'Quality Gate passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'PENDING',
                       description: 'Packaging', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        sh "./gradlew --no-daemon clean build"
        archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: true
      }
      post {
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'SUCCESS',
                       description: 'Artifact built', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
        failure {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'FAILURE',
                       description: 'Packaging failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
        }
      }
    }
  }

  post {
    success {
      githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci', status: 'SUCCESS',
                   description: 'Pipeline passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
    }
    failure {
      githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci', status: 'FAILURE',
                   description: 'Pipeline failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT
    }
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}