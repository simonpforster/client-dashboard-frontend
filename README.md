
# Client Dashboard Service

This service allows a client to login using their password and crn after logging in with their correct details, they are taken to the dashboard this is where the client can add an agent to their account after entereing a agents ARN into the dashboard. while logged in the client can also alter their account details as well as deleting and signing out of their account, this service consists of the following pages:

- Home Page
- Login Page
- Dashboard Page
- Delete Pages:
  - Delete Are you sure Page
  - Delete Success Page
- Update Pages:
  - Update  Client Summary Page
  - Update  Client Name Page
  - Update  Client Contact Number Page
  - Update  Client Property Page
  - Update  Client Business Type Page
- Property Page
- Password page

####Standards:
To keep the pages accessible to a wide audience, the service follows hmrc guidlines through
the whole service keeping uniformity throughout.

### How to Setup/run the service:
To run this service you must have the following setup:
- Have nothing running on ports (9007,9006,9008)
- Have scala version: 2.12.13
- Have sbt installed on your computer
- Have Client-Backend running this uses port (9006)
- Have Client-Registration-Frontend running this uses port (9007)

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
