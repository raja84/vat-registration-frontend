# vat-registration-frontend

[![Build Status](https://travis-ci.org/hmrc/vat-registration-frontend.svg)](https://travis-ci.org/hmrc/vat-registration-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-registration-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-registration-frontend/_latestVersion)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Prior to committing
```
sbt clean coverage test it:test scalastyle coverageReport
```
alternatively, create an alias for the above line, and get in the habit of running it before checking in:

```bash
alias precommit="sbt clean coverage test it:test scalastyle coverageReport"
```

### NOTE: Only commit if test coverage report is above or equal to 95%, scalastyle warnings are corrected and tests green.

## Running locally

User service manager to run all services required by VAT Registration Frontend:

```
sm --start VAT_REG_DEP -f
```

Note there is also VAT_REG_ALL profile that will run all dependencies plus the vat registration frontend itself.

Alternatively, create an alias for starting the services required for the VAT Registration Frontend

```bash
alias scrs='sm --start AUTH AUTH_LOGIN_STUB AUTHENTICATOR CA_FRONTEND GG GG_STUBS USER_DETAILS KEYSTORE SAVE4LATER DATASTREAM ASSETS_FRONTEND -f'
```

To run the service, just `cd` to cloned directory and execute:

```
sbt run
```

To run with the test-only endpoints, then execute the following command:
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
### Nginx
if you want to avoid having to include the service port number in the URL, add a rule to your nginx config.

```
location /register-for-vat {
  proxy_pass                http://localhost:9895;
}
```

Link to app running locally: 

http://localhost/register-for-vat/

