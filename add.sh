#!/bin/sh

read -p "email: " email
echo 'password:'
read -s password

curl -s -XPOST "https://prod-nginz-https.wire.com/login" \
    -H 'Content-Type: application/json' \
    -d '{"email":"'${email}'"
        ,"password":"'${password}'"}' \
    | jq -r ".access_token" > .token

token=$(cat .token)

curl -s -XPOST "https://prod-nginz-https.wire.com/conversations" \
    -H 'Content-Type: application/json' \
    -H 'Authorization:Bearer '${token}'' \
    -d '{"users": [], "name":"Texas Hold''em"}' \
    | jq -r '.id' > .conv

service='4a9837f9-b604-4ba4-a3a0-5eef53e7e528'
provider='d39b462f-7e60-4d88-82e1-44d632f94901'

conv=$(cat .conv)
curl -i -XPOST 'https://prod-nginz-https.wire.com/conversations/'${conv}'/bots' \
    -H 'Content-Type: application/json' \
    -H 'Authorization:Bearer '${token}'' \
    -d '{"service": "'${service}'", "provider": "'${provider}'"}'
