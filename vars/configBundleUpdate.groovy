// vars/configBundleUpdate.groovy
def call(String nameSpace = "cloudbees-core") {
  def masterName = System.properties.'MASTER_NAME'
  def label = "kubectl"
  def podYaml = libraryResource 'podtemplates/kubectl.yml'
  
  podTemplate(name: 'kubectl', label: label, yaml: podYaml) {
    node(label) {
      checkout scm
      container("kubectl") {
        sh "mkdir -p ${masterName}"
        sh "cp *.yaml ${masterName}"
        sh "kubectl cp --namespace ${nameSpace} ${masterName} cjoc-0:/var/jenkins_home/jcasc-bundles-store/ -c jenkins"
      }
      echo "begin config bundle reload"
      sh "curl -O https://raw.githubusercontent.com/cloudbees-days/ops-workshop-setup/master/groovy/reload-casc.groovy"
      sh "curl -O http://teams-${masterName}/teams-${masterName}/jnlpJars/jenkins-cli.jar"
      withCredentials([usernamePassword(credentialsId: 'admin-cli-token', usernameVariable: 'JENKINS_CLI_USR', passwordVariable: 'JENKINS_CLI_PSW')]) {
          sh """
            alias cli='java -jar jenkins-cli.jar -s http://teams-${masterName}/teams-${masterName}/ -auth $JENKINS_CLI_USR:$JENKINS_CLI_PSW'
            cli groovy =<./reload-casc.groovy
          """
      }
    }
  }
}
