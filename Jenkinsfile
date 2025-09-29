pipeline {
  agent any

  tools {
    jdk 'jdk21'          // Настрой в Jenkins: Manage Jenkins → Tools → JDK installations
  }

  environment {
    GRADLE_USER_HOME = "${WORKSPACE}/.gradle"

    // Jenkins -> Manage Jenkins -> System -> SonarQube servers (name)
    SONARQUBE_SERVER = 'sonarqube-server'

    // Jenkins credentials: GitHub PAT (repo:status) -> ID
    GITHUB_CREDENTIALS = '08fbc2eb-0bd3-4593-89e7-a73d887e8ab9'
  }

  stages {
    stage('Checkout') {
      steps {
        script {
          // 1) checkout и фиксация SHA
          def scmVars = checkout scm
          env.GIT_COMMIT = scmVars.GIT_COMMIT

          // 2) origin URL → repoUrl (HTTPS)
          def remote = sh(script: 'git config --get remote.origin.url', returnStdout: true).trim()
          def repoUrl = remote
          if (remote.startsWith('git@')) {
            // git@github.com:owner/repo(.git) -> https://github.com/owner/repo
            def path = remote.split(':', 2)[1]
            if (path.endsWith('.git')) path = path.substring(0, path.length() - 4)
            repoUrl = "https://github.com/${path}"
          } else if (remote.endsWith('.git')) {
            repoUrl = remote.substring(0, remote.length() - 4)
          }
          env.REPO_URL = repoUrl
          echo "Repo URL for notifications: ${env.REPO_URL}"
        }

        // стартовый общий статус
        script {
          try {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci', status: 'PENDING',
              description: 'Pipeline started', targetUrl: env.BUILD_URL
          } catch (e) { echo "githubNotify (ci start) skipped: ${e}" }
        }
      }
    }

    stage('Build') {
      steps {
        script {
          try {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/build', status: 'PENDING',
              description: 'Build running', targetUrl: env.BUILD_URL
          } catch (e) { echo "githubNotify (build start) skipped: ${e}" }
        }
        sh "./gradlew --no-daemon clean assemble -x test"
      }
      post {
        success {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/build', status: 'SUCCESS',
              description: 'Build passed', targetUrl: env.BUILD_URL
          }
        }
        failure {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/build', status: 'FAILURE',
              description: 'Build failed', targetUrl: env.BUILD_URL
          }
        }
      }
    }

    stage('Tests') {
      steps {
        script {
          try {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/tests', status: 'PENDING',
              description: 'Tests running', targetUrl: env.BUILD_URL
          } catch (e) { echo "githubNotify (tests start) skipped: ${e}" }
        }
        sh "./gradlew --no-daemon test jacocoTestReport"
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
          archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
        }
        success {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/tests', status: 'SUCCESS',
              description: 'Tests passed', targetUrl: env.BUILD_URL
          }
        }
        failure {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/tests', status: 'FAILURE',
              description: 'Tests failed', targetUrl: env.BUILD_URL
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        script {
          try {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/sonarqube', status: 'PENDING',
              description: 'SonarQube analysis running', targetUrl: env.BUILD_URL
          } catch (e) { echo "githubNotify (sonar start) skipped: ${e}" }
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
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
              try {
                githubNotify credentialsId: env.GITHUB_CREDENTIALS,
                  repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
                  context: 'ci/sonarqube', status: 'FAILURE',
                  description: "Quality Gate: ${qg.status}", targetUrl: env.BUILD_URL
              } catch (e) { echo "githubNotify (sonar fail) skipped: ${e}" }
              error "Quality Gate: ${qg.status}"
            }
          }
        }
      }
      post {
        success {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/sonarqube', status: 'SUCCESS',
              description: 'Quality Gate passed', targetUrl: env.BUILD_URL
          }
        }
      }
    }

    stage('Package') {
      when { branch 'main' }
      steps {
        script {
          try {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/package', status: 'PENDING',
              description: 'Packaging', targetUrl: env.BUILD_URL
          } catch (e) { echo "githubNotify (package start) skipped: ${e}" }
        }
        sh "./gradlew --no-daemon clean build"
        archiveArtifacts artifacts: 'build/libs/*.jar', onlyIfSuccessful: true
      }
      post {
        success {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/package', status: 'SUCCESS',
              description: 'Artifact built', targetUrl: env.BUILD_URL
          }
        }
        failure {
          script {
            githubNotify credentialsId: env.GITHUB_CREDENTIALS,
              repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
              context: 'ci/package', status: 'FAILURE',
              description: 'Packaging failed', targetUrl: env.BUILD_URL
          }
        }
      }
    }
  }

  post {
    success {
      script {
        githubNotify credentialsId: env.GITHUB_CREDENTIALS,
          repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
          context: 'ci', status: 'SUCCESS',
          description: 'Pipeline passed', targetUrl: env.BUILD_URL
      }
    }
    failure {
      script {
        githubNotify credentialsId: env.GITHUB_CREDENTIALS,
          repoUrl: env.REPO_URL, sha: env.GIT_COMMIT,
          context: 'ci', status: 'FAILURE',
          description: 'Pipeline failed', targetUrl: env.BUILD_URL
      }
    }
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}