How to sign CSAR file
---------------------
openssl req -new -nodes -x509 -keyout root-private-key.pem > root.cert
openssl req -new -nodes -keyout sample-pnf-private-key.pem > sample-pnf-request.pem
openssl x509 -req -CA root.cert -CAkey root-private-key.pem -CAcreateserial < sample-pnf-request.pem > sample-pnf.cert
openssl cms -sign -binary -nocerts -outform pem -signer sample-pnf.cert -inkey sample-pnf-private-key.pem < sample-pnf.csar > sample-pnf.cms