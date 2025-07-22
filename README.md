# RocktheJVMOAuthTest
A small application to test O auth and Ory

Depending on which routes you want to use, you'll need to set up an app in Github and/or Google, so as to be able to get the required client_ID
and secret credentials. The credentials acquired from google can be used for _both_ the googlecallback and orycallback routes, but are configured in different files.

To use the `githubcallback` OR `googlecallback` routes add you credentials to [`resources/appConfig.json`](./src/main/resources/appConfig.json).

To use the orycallback route add your (google) credentials to [`contrib/quickstart/kratos/email-password/kratos.yml`](./contrib/quickstart/kratos/email-password/kratos.yml).

You'll also need to configure the callback routes in the created apps.

Finally, you'll need to update the client_id links in the first two links in [`index.html`](./src/main/resources/html/index.html) to point to your own client_ids.

### Once you have everything in place:

Run `docker-compose -f quickstart.yml -f quickstart-standalone.yml up --force-recreate` 
in the project root in a terminal.

You can also start docker to run against postgres, as it's easier to just delete yourself from the db. Just run
`docker-compose -f quickstart.yml -f quickstart-standalone.yml -f quickstart-postgres.yml up --build --force-recreate`

Then in sbt run the `OAuthDemo` app. You should then see your links when navigating to `localhost:8080/home`.

The below flow steps were copied from the [Rock the JVM](https://rockthejvm.com/articles/authentication-with-scala-and-http4s-oauth) 
docs and are referring to the `callback` and `googlecallback` routes in [`OAuthDemo.scala`](./src/main/scala/com/oAuth/OAuthDemo.scala)
They may not be completely accurate for the `orycallback` route flow.

1. When the user tries to log into app1, the user is redirected to an authorization server owned by app2.

2. The authorization server provides the user with a prompt, asking the user to grant app1 access to app2 with a list of permissions.

3. Once the prompt is accepted, the user is redirected back to app1 with a single-use authorization code.

4. app1 will respond to app2 with the same authorization code, a client id, and a client secret.

5. The authorization server on app2 will respond with a token id and an access token

6. app1 can now request the user’s information from app2’s API using the access token.

