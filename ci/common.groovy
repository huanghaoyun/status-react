def installJSDeps() {
  def attempt = 1
  def maxAttempts = 10
  def installed = false
  while (!installed && attempt <= maxAttempts) {
    println "#${attempt} attempt to install npm deps"
    sh 'scripts/prepare-for-platform.sh desktop'
    sh 'npm install'
    installed = fileExists('node_modules/web3/index.js')
    attemp = attempt + 1
  }
}

def doGitRebase() {
  try {
    sh 'git rebase origin/develop'
  } catch (e) {
    sh 'git rebase --abort'
    throw e
  }
}

def tagBuild() {
  withCredentials([[
    $class: 'UsernamePasswordMultiBinding',
    credentialsId: 'status-im-auto',
    usernameVariable: 'GIT_USER',
    passwordVariable: 'GIT_PASS'
  ]]) {
    return sh(
      returnStdout: true,
      script: './scripts/build_no.sh --increment'
    ).trim()
  }
}

return this
