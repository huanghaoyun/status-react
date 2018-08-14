common = load 'ci/common.groovy'

def uploadArtifact() {
  def artifact_dir = pwd() + '/android/app/build/outputs/apk/release/'
  println (artifact_dir + 'app-release.apk')
  def artifact = (artifact_dir + 'app-release.apk')
  def server = Artifactory.server('artifacts')
  shortCommit = sh(
    returnStdout: true,
    script: 'git rev-parse HEAD'
  ).trim().take(6)
  def filename = "im.status.ethereum-${shortCommit}-n-fl.apk"
  def newArtifact = (artifact_dir + filename)
  sh "cp ${artifact} ${newArtifact}"
  def uploadSpec = '{ "files": [ { "pattern": "*apk/release/' + filename + '", "target": "nightlies-local" }]}'
  def buildInfo = server.upload(uploadSpec)
  return 'http://artifacts.status.im:8081/artifactory/nightlies-local/' + filename
}

def prepDeps() {
  sh 'git fetch --tags'
  sh 'rm -rf node_modules'
  sh 'cp .env.nightly .env'
  /* prepare environment for specific platform build */
  sh 'scripts/prepare-for-platform.sh mobile'
  version = readFile("${env.WORKSPACE}/VERSION").trim()
  common.installJSDeps()
  sh 'mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack'
  // TODO sh 'cd ios && pod install'
}

def compileAndroid() {
  withCredentials([
    string(credentialsId: "SUPPLY_JSON_KEY_DATA", variable: 'GOOGLE_PLAY_JSON_KEY'),
    string(credentialsId: "SLACK_URL", variable: 'SLACK_URL')
  ]) {
    sh ('bundle exec fastlane android nightly')
  }
}

def runTests() {
  sh 'lein test-cljs'
}

def leinBuild() {
  sh 'lein prod-build'
}

def buildAndroid() {
  dir('android') {
    sh './gradlew react-native-android:installArchives'
    sh './gradlew assembleRelease'
  }

  //stage('Build (Android) for e2e tests') {
  //  dir('android') {
  //    sh """
  //      mv app/build/outputs/apk/release/app-release.apk \\
  //         app/build/outputs/apk/release/app-release.original.apk
  //    """
  //    env.ENVFILE=".env.e2e"
  //    sh './gradlew assembleRelease'
  //  }
  //}
}

def compileiOS() {
  withCredentials([
    string(credentialsId: "SLACK_URL", variable: 'SLACK_URL'),
    string(credentialsId: "slave-pass-${env.NODE_NAME}", variable: 'KEYCHAIN_PASSWORD'),
    string(credentialsId: 'FASTLANE_PASSWORD', variable: 'FASTLANE_PASSWORD'),
    string(credentialsId: 'APPLE_ID', variable: 'APPLE_ID'),
    string(credentialsId: 'fastlane-match-password', variable:'MATCH_PASSWORD')
  ]) {
    sh "plutil -replace CFBundleShortVersionString  -string ${version} ios/StatusIm/Info.plist"
    sh "plutil -replace CFBundleVersion -string ${build_no} ios/StatusIm/Info.plist"
    sh 'bundle exec fastlane ios nightly'
  }
}

return this
