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
        script {
          // 1) checkout
          def scmVars = checkout scm
          env.GIT_COMMIT = scmVars.GIT_COMMIT

          // 2) origin URL
          def remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
          echo "origin: ${remote}"

          // 3) Парсим owner/repo БЕЗ regex
          String owner = null, repo = null

          if (remote?.startsWith('git@')) {
            // git@github.com:owner/repo.git
            def parts = remote.split(':', 2)
            if (parts.length == 2) {
              def path = parts[1]
              if (path.endsWith('.git')) path = path.substring(0, path.length() - 4)
              def slash = path.indexOf('/')
              if (slash > 0) {
                owner = path.substring(0, slash)
                repo  = path.substring(slash + 1)
              }
            }
          } else if (remote?.startsWith('http')) {
            // https://github.com/owner/repo(.git)
            def url = new java.net.URL(remote)
            def path = url.getPath()
            if (path.startsWith('/')) path = path.substring(1)
            if (path.endsWith('.git')) path = path.substring(0, path.length() - 4)
            def slash = path.indexOf('/')
            if (slash > 0) {
              owner = path.substring(0, slash)
              repo  = path.substring(slash + 1)
            }
          }

          if (owner && repo) {
            env.GH_OWNER = owner
            env.GH_REPO  = repo
            echo "GitHub repo detected: ${env.GH_OWNER}/${env.GH_REPO} @ ${env.GIT_COMMIT}"
          } else {
            env.GH_NOTIFY_DISABLED = 'true'
            echo "WARN: Не удалось распарсить GitHub slug из ${remote} — githubNotify будет отключён"
          }

          // 4) Общий стартовый статус (если парсинг успешен)
          if (env.GH_NOTIFY_DISABLED != 'true') {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
              context: 'ci', status: 'PENDING',
              description: 'Pipeline started', targetUrl: env.BUILD_URL
          }
        }
      }
    }

    stage('Build') {
      steps {
        script {
          if (env.GH_NOTIFY_DISABLED != 'true') {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
              context: 'ci/build', status: 'PENDING',
              description: 'Build running', targetUrl: env.BUILD_URL
          }
        }
        sh "./gradlew --no-daemon clean assemble -x test"
      }
      post {
        success {
          script {
            if (env.GH_NOTIFY_DISABLED != 'true') {
              githubNotify credentialsId: env.GITHUB_CREDENTIALS,
                account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
                context: 'ci/build', status: 'SUCCESS',
                description: 'Build passed', targetUrl: env.BUILD_URL
            }
          }
        }
        failure {
          script {
            if (env.GH_NOTIFY_DISABLED != 'true') {
              githubNotify credentialsId: env.GITHUB_CREDENTIALS,
                account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
                context: 'ci/build', status: 'FAILURE',
                description: 'Build failed', targetUrl: env.BUILD_URL
            }
          }
        }
      }
    }

    stage('Tests') {
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/tests', status: 'PENDING',
                       description: 'Tests running', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
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
                       description: 'Tests passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
        failure {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/tests', status: 'FAILURE',
                       description: 'Tests failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
      }
    }


stage('SonarQube Analysis') {
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/sonarqube', status: 'PENDING',
                       description: 'SonarQube analysis running', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
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
                           description: "Quality Gate: ${qg.status}", targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                           account: env.GH_OWNER, repo: env.GH_REPO
              error "Quality Gate: ${qg.status}"
            }
          }
        }
      }
      post {
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/sonarqube', status: 'SUCCESS',
                       description: 'Quality Gate passed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        script {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'PENDING',
                       description: 'Packaging', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
        sh "./gradlew --no-daemon clean build"
        archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: true
      }
      post {
        success {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'SUCCESS',
                       description: 'Artifact built', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
        failure {
          githubNotify credentialsId: GITHUB_CREDENTIALS, context: 'ci/package', status: 'FAILURE',
                       description: 'Packaging failed', targetUrl: env.BUILD_URL, sha: env.GIT_COMMIT,
                       account: env.GH_OWNER, repo: env.GH_REPO
        }
      }
    }
  }

  post {
    success {
      script {
        if (env.GH_NOTIFY_DISABLED != 'true') {
          githubNotify credentialsId: env.GITHUB_CREDENTIALS,
            account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
            context: 'ci', status: 'SUCCESS',
            description: 'Pipeline passed', targetUrl: env.BUILD_URL
        }
      }
    }
    failure {
      script {
        if (env.GH_NOTIFY_DISABLED != 'true') {
          githubNotify credentialsId: env.GITHUB_CREDENTIALS,
            account: env.GH_OWNER, repo: env.GH_REPO, sha: env.GIT_COMMIT,
            context: 'ci', status: 'FAILURE',
            description: 'Pipeline failed', targetUrl: env.BUILD_URL
        }
      }
    }
  }
}