#!/bin/bash

VARIABLES_SHELL_SCRIPT_FILE_PATH=$(dirname "$0")/variables.sh
MANAGED_APP_ID_CONFIG_FILE_PATH=$(dirname "$0")/applications.txt

CURL_LOGS_DIRECTORY=$(dirname "$0")/logs
# create logs directory
mkdir -p "${CURL_LOGS_DIRECTORY}"

# $1 is managed app id
# other variables are referenced from outside of function
function authorizeManagedAppId() {
  local MANAGED_APP_ID=$1
  local LOG_PATH="${CURL_LOGS_DIRECTORY}/${MANAGED_APP_ID}.log"
  echo -e "\n$(date -R)\n" > "${LOG_PATH}"
  curl -vs --request POST \
  --header "Cookie: JSESSIONID=${JSESSIONID}" \
  --header "Content-Type: application/json;charset=UTF-8" \
  --data-raw $(printf '{"appId":"%s"}' "${MANAGED_APP_ID}") \
  "http://${APOLLO_PORTAL_IP}:${APOLLO_PORTAL_PORT}/consumers/${APOLLO_OPEN_API_TOKEN}/assign-role?type=AppRole" \
  >> "${LOG_PATH}" 2>&1
}

# import variables
source "${VARIABLES_SHELL_SCRIPT_FILE_PATH}"

# get all app id from file
managedAppIds=$(grep -v -e '^[[:space:]]*$' "${MANAGED_APP_ID_CONFIG_FILE_PATH}")

# traversal app id
for managedAppId in ${managedAppIds}
do
  managedAppIdAfterTrim=`echo "${managedAppId}" | sed "s/[[:space:]]//g"`
  authorizeManagedAppId "${managedAppIdAfterTrim}"
done