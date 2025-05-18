[![progress-banner](https://backend.codecrafters.io/progress/redis/b7dbc9e6-45a6-4033-97e0-c93c0a469df9)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This is a starting point for Java solutions to the
["Build Your Own Redis" Challenge](https://codecrafters.io/challenges/redis).

In this challenge, you'll build a toy Redis clone that's capable of handling
basic commands like `PING`, `SET` and `GET`. Along the way we'll learn about
event loops, the Redis protocol and more.

**Note**: If you're viewing this repo on GitHub, head over to
[codecrafters.io](https://codecrafters.io) to try the challenge.

# Passing the first stage

The entry point for your Redis implementation is in `src/main/java/Main.java`.
Study and uncomment the relevant code, and push your changes to pass the first
stage:

```sh
git commit -am "pass 1st stage" # any msg
git push origin master
```

That's all!

# Stage 2 & beyond

Note: This section is for stages 2 and beyond.

1. Ensure you have `mvn` installed locally
1. Run `./your_program.sh` to run your Redis server, which is implemented in
   `src/main/java/Main.java`.
1. Commit your changes and run `git push origin master` to submit your solution
   to CodeCrafters. Test output will be streamed to your terminal.

### Dockerize

```shell
docker build -t myredis .

docker run -p 6379:6379 myredis

ip route show | grep default | awk '{print $3}'

docker run -p 6480:6480 myredis --port 6480 --replicaof "<ipaddress> 6379"
```

### Self Hosted Agent with Docker capabilities

```shell
cd dind

docker build --tag "azp-agent:linux"  .

docker run --privileged -e AZP_URL="https://dev.azure.com/ChaitanyaDSharma" -e AZP_TOKEN="use your own token" -e AZP_POOL="agent" -e AZP_AGENT_NAME="Docker Agent - Linux" --name "azp-agent-linux" azp-agent:linux
```

### K8s cluster on KIND + Helm

```shell
mkdir kind
cd kind
vi k.yml
```
```yml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30007
    hostPort: 30007
```
```shell
kind create cluster --config k.yml --name cluster-name
cd ..
docker build -t beelzekamibub/redis:latest .
kind load docker-image beelzekamibub/redis:latest --name cluster-name
vi pod.yml
```
```yml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: redis
  name: redis
  namespace: default
spec:
  containers:
    - image: beelzekamibub/redis:latest
      imagePullPolicy: Never
      name: redis
```
```shell
vi service.yml
```
```yml
apiVersion: v1
kind: Service
metadata:
  labels:
    run: redis
  name: redis-service
spec:
  ports:
    - port: 6379
      protocol: TCP
      targetPort: 6379
      nodePort: 30007
  selector:
    run: redis
  type: NodePort
```
```shell
alias k=kubectl
k apply -f pod.yml
k apply -f service.yml
# run ise
helm create redis-chart
```

clean out templates folder, delete charts folder, as no dependencies

Move the pod.yml and service.yml to 

templatize the yaml files and add to values.yml

add annotations for helm hook

```shell
helm template redis-chart
helm lint redis-chart
helm install redis-app redis-chart

helm uninstall redis-app
```