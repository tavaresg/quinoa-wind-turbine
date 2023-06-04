
# Java App com Quarkus Demo

Este projeto usa o Quarkus com as seguintes extensões:
    - RESTEasy Reactive Jackson
    - Elytron Security Properties File
    - Quinoa
    - SmallRye OpenAPI
    - RESTEasy Reactive
    - SmallRye Reactive Messaging - Kafka Connector
    - OpenShift
    - Kubernetes Service Binding
    - SmallRye Health
    - Infinispan Client

## Rodando o App com o Developer Mode do Quarkus

Pre-requisitos:
- JDK 17
- Maven
- Quarkus CLI

Executar app no dev mode que habilita o live coding
```shell script
quarkus dev
```

## Deploy no Cluster OpenShift

### Setup

Para executar é preciso ter um cluster **OpenShift >=4.10** com privilégios de cluster-admin.

#### Repositório Quay.io

Criar repositório no Quay.io:

* quinoa-wind-turbine

Marcar como **Public Repository**

### Configurar OpenShift

Logar no Web Console do OpenShift e instalar pre-requisitos

Primeiro, criar projeto para a demo:
```bash
oc new-project demo --description='wind-turbine-race'
```
*Você pode escolher um nome diferente, só terá que alterar depois o arquivo `argo/wind-turbine-app.yaml`

### Instalar Operators

#### OpenShift Pipelines

Instalar OpenShift Pipelines

#### OpenShift GitOps

Instalar OpenShift GitOps

##### Add permission to Argo CD service account

**IMPORTANT** Dar permissão para a service account do Argo CD gerenciar o cluster:
```bash
oc adm policy add-cluster-role-to-user cluster-admin -z openshift-gitops-argocd-application-controller -n openshift-gitops
```

#### AMQ Streams


1. Instalar o AMQ Streams (Kafka)
2. Nomear cluster como `my-cluster`

#### Infinispan

1. Criar um cluster infinispan via Helm chart pesquisando no Catalogo via Developer view Add+
2. Nomear cluster como `infinispan`

### Flow

Criar secret com as credenciais do Quay.io:

```bash
oc create secret docker-registry quay-secret --docker-server=quay.io --docker-username=<QUAY_USERNAME> --docker-password=<ENCRYPTED_PASSWORD>
```

Criar secret com as credenciais do GitHub:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: git-user-pass
  annotations:
    tekton.dev/git-0: https://github.com
type: kubernetes.io/basic-auth
stringData:
  username: <github user>
  password: <github personal access token>
```

```bash
oc create -f git-user-pass.yaml
```

Linkar secrets com service account do OpenShift Pipelines 

```bash
oc secret link pipeline quay-secret
oc secret link pipeline git-user-pass
```

Criar pipeline via Tekton pipeline manifests:

Create Tekton pipeline manifests

Forkar repo do Git e clonar para dir local
```bash
git clone https://github.com/your-git/quinoa-wind-turbine
git clone https://github.com/your-git/quinoa-wind-turbine-manifests
```

Alterar usuário do Quay e do GitHub no arquivo da pipeline `tekton/pipeline-cached.yaml`, buscar por:
* rhdevelopers
* redhat-developer-demos

Criar pipeline

```bash
oc create -f app-source-pvc.yaml
oc create -f build-cache-pvc.yaml
oc create -f el-route.yaml
oc create -f eventlistener.yaml
oc create -f git-update-deployment.yaml
oc create -f maven-task-cached.yaml
oc create -f pipeline-cached.yaml
oc create -f triggerbinding.yaml
oc create -f triggertemplate-cached.yaml
```

Atualizar referências em `k8s/deployment.yaml` e `argo/wind-turbine-app.yaml`

Criar App via Argo CD

```bash
oc apply -f argo/wind-turbine-app.yaml
```


### SSL (para o caso o cluster não possua certificado):

Let'encrypt:
```bash
oc apply -fhttps://raw.githubusercontent.com/tnozicka/openshift-acme/master/deploy/single-namespace/{role,serviceaccount,issuer-letsencrypt-live,deployment}.yaml
oc create rolebinding openshift-acme --role=openshift-acme --serviceaccount="$( oc project -q ):openshift-acme" --dry-run -o yaml | oc apply -f -
```