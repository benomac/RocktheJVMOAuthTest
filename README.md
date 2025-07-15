# RocktheJVMOAuthTest
A small application to test O auth 


1. When the user tries to log into app1, the user is redirected to an authorization server owned by app2.

2. The authorization server provides the user with a prompt, asking the user to grant app1 access to app2 with a list of permissions.

3. Once the prompt is accepted, the user is redirected back to app1 with a single-use authorization code.

4. app1 will respond to app2 with the same authorization code, a client id, and a client secret.

5. The authorization server on app2 will respond with a token id and an access token

6. app1 can now request the user’s information from app2’s API using the access token.