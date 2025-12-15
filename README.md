Termux:
Enable Storage Access:
termux-setup-storage

magkakaroon ng folder na: /storage/emulated/0

CD to
cd /storage/emulated/0/Documents

mkdir myapp
cd myapp


mvn archetype:generate -DgroupId=com.tymex \
  -DartifactId=myapp \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false 


iset nadin sa JStudio ang Directory
/storage/emulated/0/Documents/truepa/myapp
or simply Documents/truepa/myapp


mvn clean package -DskipTests
produces target/myapp-0.0.1-SNAPSHOT.jar

run the app
java -jar target/myapp-0.0.1-SNAPSHOT.jar
________________________________________________________________________________________
echo "# tymex" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/edtayum-coder/tymex.git
git push -u origin main

________________________________________________________________________________________

Let's test Problem #1:
test 1 - without idempotency key
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc1", "amount":1000}'
returns something like:
{"transactionId":"0600a11f-6195-4e33-a21f-73f76cb79c8b","status":"SUCCESS","amount":1000}.../myapp/


test 2 - with same idempotency key run once, on the second run it should return the same response as 1st run. Use the idempotency key from the test 1 response as pattern UUID.
curl -X POST http://localhost:8080/payments \
  -H "Idempotency-Key: 0600a11f-6195-4e33-a21f-73f76cb79c8b" \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc1", "amount":1000}'


test 3 - with different idempotency key, will produce new set of response
curl -X POST http://localhost:8080/payments \
  -H "Idempotency-Key: test-999" \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc1", "amount":1000}'


test 4 - rapid concurrency test
curl -X POST http://localhost:8080/payments \
  -H "Idempotency-Key: fast-555" \
  -H "Content-Type: application/json" \
  -d '{"accountId":"acc1", "amount":500}'


Let's test Problem #2:
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"userId":"123", "message":"Hello Edwin!"}'

