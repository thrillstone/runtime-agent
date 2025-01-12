#!/usr/bin/env bash

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

usage() {
  cat <<EOF
Usage: $(basename "${BASH_SOURCE[0]}") [-h] [-v] -t IMAGE_TAG [-bt docker_base_image_tag]

Creates a docker image of the runtime agent. The runtime agent docker image is made up
of a base image and the runtime-agent application layer. The base image tag can be specified
using the bt parameter. If the base image tag exists, it is used, if it does not exist, it
is built automatically before creating the application docker image.

Available options:

-h, --help      Print this help and exit
-v, --verbose   Print script debug info
-t, --tag      The docker container tag
-bt, --basetag     The docker container tag of the base image
EOF
  exit
}

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

setup_colors() {
  if [[ -t 2 ]] && [[ -z "${NO_COLOR-}" ]] && [[ "${TERM-}" != "dumb" ]]; then
    NOFORMAT='\033[0m' RED='\033[0;31m' GREEN='\033[0;32m' ORANGE='\033[0;33m' BLUE='\033[0;34m' PURPLE='\033[0;35m' CYAN='\033[0;36m' YELLOW='\033[1;33m'
  else
    NOFORMAT='' RED='' GREEN='' ORANGE='' BLUE='' PURPLE='' CYAN='' YELLOW=''
  fi
}

msg() {
  echo >&2 -e "${1-}"
}

die() {
  local msg=$1
  local code=${2-1} # default exit status 1
  msg "$msg"
  exit "$code"
}

parse_params() {
  # default values of variables set from params
  flag=0
  param=''

  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --verbose) set -x ;;
    --no-color) NO_COLOR=1 ;;
    -t | --tag)  # the docker tag
      IMAGE_TAG="${2-}"
      shift
      ;;
    -bt | --basetag) # example named parameter
      BASE_IMAGE_TAG="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  # check required params and arguments
  [[ -z "${IMAGE_TAG-}" ]] && die "Missing required parameter: tag"
  #[[ ${#args[@]} -eq 0 ]] && die "Missing script arguments"

  return 0
}

parse_params "$@"
setup_colors

# If the base docker container tag isn't set, use the same tag that was
# provided for the runtime-agent.

[[ -z "${BASE_IMAGE_TAG-}" ]] && BASE_IMAGE_TAG=${IMAGE_TAG}

if [[ "$(docker images -q runtime-agent-base:${BASE_IMAGE_TAG} 2> /dev/null)" == "" ]]; then
  # do something
  msg "${RED}The base image does not exist, building\n${NOFORMAT}"
  cd base-image
  ./buildBaseImage.sh ${BASE_IMAGE_TAG}
  cd ${script_dir}
else
  msg "${GREEN}Base image found, using:${YELLOW} runtime-agent-base:${BASE_IMAGE_TAG}\n${NOFORMAT}"
fi

msg "${GREEN}Building image:${YELLOW} runtime-agent:${IMAGE_TAG}\n${NOFORMAT}"

export BASE_IMAGE=runtime-agent-base:${BASE_IMAGE_TAG}
export GITHASH=$(git rev-parse HEAD)
export GITBRANCH=$(git branch --show-current)
export BUILD_TIMESTAMP=$(date -u)
cp ../target/runtime-agent-0.0.1-SNAPSHOT.jar .

cd ..
docker build docker -t runtime-agent:${IMAGE_TAG} --build-arg BASE_IMAGE=${BASE_IMAGE}\
       --build-arg JAR_FILE=runtime-agent-0.0.1-SNAPSHOT.jar --build-arg GITHASH=${GITHASH}\
       --build-arg BUILD_TIMESTAMP="${BUILD_TIMESTAMP}" --build-arg GITBRANCH=${GITBRANCH}
cd ${script_dir}

# cleanup
rm runtime-agent-0.0.1-SNAPSHOT.jar