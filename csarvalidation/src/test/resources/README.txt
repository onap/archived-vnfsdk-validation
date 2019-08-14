How to sign CSAR file - option 2
---------------------
openssl req -new -nodes -x509 -keyout root-private-key.pem > root.cert
openssl req -new -nodes -keyout sample-pnf-private-key.pem > sample-pnf-request.pem
openssl x509 -req -CA root.cert -CAkey root-private-key.pem -CAcreateserial < sample-pnf-request.pem > sample-pnf.cert
openssl cms -sign -binary -nocerts -outform pem -signer sample-pnf.cert -inkey sample-pnf-private-key.pem < sample-pnf.csar > sample-pnf.cms

How to sign CSAR file - option 1
--------------------------------
openssl req -new -nodes -x509 -keyout root-private-key.pem > root-certificate.cert
TIP: As a 'Organizational Unit Name' set, for example: Certificate Authority

openssl req -new -nodes -keyout signing-private-key.pem > signing-request.pem
TIP: As a 'Organizational Unit Name' set, for example: Nokia. Name values must be different!

openssl x509 -req -CA root-certificate.cert -CAkey root-private-key.pem -CAcreateserial < signing-request.pem > signing-certificate.cert

openssl cms -sign -signer signing-certificate.cert -inkey signing-private-key.pem -outform pem -binary < MainServiceTemplate.mf > signature-and-certificate.cms

openssl cms -verify -content MainServiceTemplate.mf -CAfile root-certificate.cert -inform pem -binary < signature-and-certificate.cms > /dev/null
