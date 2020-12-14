#!/bin/bash -e

if ! which jq > /dev/null; then
  echo "jq is required" >&2
  exit 1
fi

if [[ $# -ne 3 ]]; then
  echo "$(basename "$0")
Assume an AWS IAM role and store the credentials as an AWS CLI profile

usage: $(basename "$0")  from_profile  role_arn  new_profile

    from_profile  - name of the AWS CLI profile to use to assume the role
    role_arn      - ARN of the role to assume
    new_profile   - name of the AWS CLI profile in which to store the acquired credentials
" >&2
  exit 1
fi

FROM_PROFILE=$1
ROLE=$2
NEW_PROFILE=$3

ROLE_NAME=$(echo "$ROLE" | sed 's#.*/##; s#Role$##') # Get the role name from the ARN and remove "Role" from the end
SESSION_NAME="$ROLE_NAME"Session
DURATION=3600 # 1 hour
ASSUME_ROLE_OUTPUT_FILE=/tmp/aws-assume-role-output

echo "Assuming role $ROLE" >&2
aws \
    --profile "$FROM_PROFILE" \
    sts assume-role \
    --role-arn "$ROLE" \
    --role-session-name "$SESSION_NAME" \
    --duration-seconds "$DURATION" \
    > "$ASSUME_ROLE_OUTPUT_FILE"

# shellcheck disable=SC2064
trap "{ rm -f $ASSUME_ROLE_OUTPUT_FILE; }" exit

echo "Setting credentials for '$NEW_PROFILE' profile..." >&2
aws configure set --profile "$NEW_PROFILE" aws_access_key_id "$(jq -r '.Credentials.AccessKeyId' < $ASSUME_ROLE_OUTPUT_FILE)"
aws configure set --profile "$NEW_PROFILE" aws_secret_access_key "$(jq -r '.Credentials.SecretAccessKey' < $ASSUME_ROLE_OUTPUT_FILE)"
aws configure set --profile "$NEW_PROFILE" aws_session_token "$(jq -r '.Credentials.SessionToken' < $ASSUME_ROLE_OUTPUT_FILE)"
aws configure set --profile "$NEW_PROFILE" region ap-southeast-2
