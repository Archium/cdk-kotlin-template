# CDK Kotlin Template

This is a template for creating [AWS CDK](https://aws.amazon.com/cdk/) definitions using Kotlin. 

The repository is mainly the output of following the instructions in AWS' ["Working with the AWS CDK in Java"](https://docs.aws.amazon.com/cdk/latest/guide/work-with-cdk-java.html) article, converted into a working Kotlin project, with demonstration of some Kotlin idioms that might help you in creating more readable CDK code.

It also includes some helpful scripts and instructions for working with CDK in a multi-account environment where access is controlled by assuming roles.

## Requirements

Tools for building:
* Java
* Maven

Tools for running:
* An AWS account (or accounts)
* [CDK](https://docs.aws.amazon.com/cdk/latest/guide/home.html) (`npm install -g aws-cdk`)
* [AWS CLI](https://aws.amazon.com/cli/) (https://awscli.amazonaws.com/AWSCLIV2.pkg)
* [jq](https://stedolan.github.io/jq/) (`brew install jq`)
* [JEnv](https://www.jenv.be/) (recommended) (`brew install jenv`)

IAM setup for running:
* A role (assumed to be called `CdkBootstrapRole` in commands below) that has the permissions required to bootstrap CDK in an AWS environment. The AWS-managed policy `AdministratorAccess` will suffice, but you may decide to be more restrictive. 
* A role (assumed to be called `CdkDeploymentRole` in commands below) that has the permissions required to run your CDK deployment operations. The AWS-managed policy `PowerUserAccess` should suffice for CDK definitions that don't perform IAM operations, but you may decide to be more restrictive, or need to be more permissive, or both.
* An AWS CLI profile with credentials to assume the CDK boostrap and deployment roles (assumed to be called "my-aws-profile" in commands below).

## Using the template

To use the template, you'll first want to change the value of the `AWS_ACCOUNT_ID` and `AWS_REGION_NAME` values in [`ExampleCdkApp`](./src/main/kotlin/com/example/cdk/ExampleCdkApp.kt)

You'll then want to add the resources that you want to create in the `ExampleStack`.

There are a small number of other `TODO:` comments around which you should find and follow.

And you probably want to choose some better names for the package and classes.

Then you can use the following operations to set up your environment using CDK.

## CDK Operations

The following instructions assume CDK is being operated in a multi-account organisation using assumed IAM roles as the primary method of access control.

If you are working with a single AWS account and are running CDK commands in an environment where the default AWS CLI user has locally stored credentials and `AdministratorAccess`-level privileges, you can probably ignore the parts of the instructions below about using the `./assume-role.sh` script and passing the `--profile` argument to CDK.

### Bootstrapping CDK

Bootstrapping only needs to be done when using CDK in an environment (account+region) for the first time.

See [the AWS docs](https://docs.aws.amazon.com/cdk/latest/guide/bootstrapping.html)
for more detail about the bootstrapping process.

The following uses the new (as at December 2020) "modern" boostrap template. 

*(You should replace `YOUR_ACCOUNT_ID` below with the ID of the account where you will be running CDK, and likewise for `YOUR_REGION`.)*

```shell script
ACCOUNT_ID=YOUR_ACCOUNT_ID
REGION=YOUR_REGION
./assume-role.sh  my-aws-profile  arn:aws:iam::$ACCOUNT_ID:role/CdkBootstrapRole  cdk-build-bootstrap

export CDK_NEW_BOOTSTRAP=1 \
    && cdk bootstrap \
    --profile cdk-build-bootstrap \
    --context bootstrap=true \
    aws://$ACCOUNT_ID/$REGION
```

### Checking your CDK definitions

*(You should replace "ACCOUNT_ID" below with the ID of the account where you will be running CDK.)*
```
./assume-role.sh  my-aws-profile  arn:aws:iam::ACCOUNT_ID:role/CdkDeploymentRole  cdk-build

cdk synth --profile cdk-build
```

### Deploying your CDK definitions

*(You should replace "ACCOUNT_ID" below with the ID of the account where you will be running CDK.)*
```
./assume-role.sh  my-aws-profile  arn:aws:iam::ACCOUNT_ID:role/CdkDeploymentRole  cdk-build

cdk deploy --profile cdk-build
```

### Other useful commands

 * `cdk ls` - list all stacks
 * `cdk diff` - compare with current state
 * `cdk docs` - open CDK documentation
 
 ## License
 
 This repository is made available under [The Unlicense](https://opensource.org/licenses/unlicense):
 
> This is free and unencumbered software released into the public domain.
> 
> Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.
> 
> In jurisdictions that recognize copyright laws, the author or authors of this software dedicate any and all copyright interest in the software to the public domain.
> We make this dedication for the benefit of the public at large and to the detriment of our heirs and successors.
> We intend this dedication to be an overt act of relinquishment in perpetuity of all present and future rights to this software under copyright law.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
> IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
