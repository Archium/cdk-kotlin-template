# CDK Kotlin Template

This is a template for creating [AWS CDK](https://aws.amazon.com/cdk/) definitions using Kotlin. 

The repository is mainly the output of following the instructions in AWS' ["Working with the AWS CDK in Java"](https://docs.aws.amazon.com/cdk/latest/guide/work-with-cdk-java.html) article, converted into a working Kotlin project, with demonstration of some Kotlin idioms that might help you in creating more readable CDK code.

It also includes some helpful scripts and instructions for working with CDK in a multi-account environment where access is controlled by assuming roles.


## Requirements

### Tools for building
* Java
* Maven


### Tools for running
* An AWS account (or accounts)
* [CDK](https://docs.aws.amazon.com/cdk/latest/guide/home.html) (`npm install -g aws-cdk`)
* [CDK Assume Role Credential Plugin](https://aws.amazon.com/blogs/devops/cdk-credential-plugin/) (`npm install -g cdk-assume-role-credential-plugin`)
* [AWS CLI](https://aws.amazon.com/cli/) (https://awscli.amazonaws.com/AWSCLIV2.pkg)
* [jq](https://stedolan.github.io/jq/) (`brew install jq`)
* [JEnv](https://www.jenv.be/) (recommended) (`brew install jenv`)


### IAM setup for running

To use CDK, you'll need:
* for bootstrapping, an account or role with `AdministractorAccess`-like privileges in the target account
* for deploying, a role which has privileges to assume the roles created by CDK during bootstrapping (named `cdk-*`)


#### IAM for a single account setup

If you're using a single account, it will be easiest if you bootstrap your CDK with an existing AWS account or role that has `AdministractorAccess` privileges.

If your normal user has `PowerUserAccess` or `AdministractorAccess`, that will be enough for deploying as well.
Otherwise, you'll need to add a policy to the users, groups or roles that you want to be able to deploy resources using CDK.
See 'CDK Deployment Role Policy' below for a suitable policy.   


#### IAM for a multi-account setup

If you're running in a multi-account setup, it will be easiest if you bootstrap your CDK with an existing AWS account or role that has `AdministractorAccess` privileges in the target account/s (where CDK will be deploying resources).
If the target account has the `OrganizationAccountAccessRole` that is set up by AWS Organisations by default with `AdministractorAccess` privileges when setting up accounts, that can be a good option to use for CDK bootstrapping.

For deploying resources with CDK, you'll need to be able to assume a role in the target account/s that can, in turn, assume the roles created by CDK's bootstrapping process.
An easy way to do this is to create a role in the target account/s (e.g. `CdkDeploymentRole`) that has the root account as the principal, and the policy defined in the 'CDK Deployment Role Policy' section below.

You'll then need to ensure that users, roles, or groups in the root account that you want to be able to deploy resources using CDK have permission to assume that role in the target account/s, for example by adding a policy like this to those users/groups/roles:
```
{
    "Version": "2012-10-17",
    "Statement": [ {
        "Effect": "Allow",
        "Action": "sts:AssumeRole",
        "Resource": "arn:aws:iam::*:role/CdkDeploymentRole"
    } ]
}
```
With all of this in place, you can use your root account user or role profile to run CDK commands, and the `cdk-assume-role-credential-plugin`, as configured in this template, will automatically assume the correct roles in the target account.

NOTE: If you use a name other than `CdkDeploymentRole` for the role in the target account/s, you'll need to update the configuration in [`cdk.context.json`](./cdk.context.json).

#### CDK Deployment Role Policy

This policy, for use in the account where CDK will be deploying resources, allows any user or role that is granted it to assume the roles create by CDK during bootstrapping.
You need to replace `ADD_TARGET_ACCOUNT_ID_HERE` in the below with your target account ID/s.
These are the only privileges needed to deploy resources using CDK.

NOTE: CDK bootstrapping is a per-region operation, and creates roles with the target region in the name.
The position of the wildcard in the policy below grants permission across *all* regions.
If you want to restrict users to only being able to run CDK against some regions and not others, you'll need a more fine-grained solution.  
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "sts:AssumeRole",
            "Resource": "arn:aws:iam::ADD_TARGET_ACCOUNT_ID_HERE:role/cdk-*"
        }
    ]
}
```


## Using the template

To use the template, you'll first want to change the value of the `AWS_ACCOUNT_ID` and `AWS_REGION_NAME` values in [`ExampleCdkApp`](./src/main/kotlin/com/example/cdk/ExampleCdkApp.kt)

You'll then want to add the resources that you want to create in the [`ExampleStack`](./src/main/kotlin/com/example/cdk/stacks/ExampleStack.kt).

There are a few other `TODO:` comments around which you should find and follow.

And you probably want to choose some better names for the package and classes.

Then you can use the following operations to set up your environment using CDK.


## CDK Operations

The following instructions assume CDK is being operated in a multi-account AWS organisation using assumed IAM roles as the primary method of access control.

If you are working with a single AWS account and are running CDK commands in an environment where the default AWS CLI user has locally stored credentials and `AdministratorAccess`-level privileges, you can probably ignore the parts of the instructions below about using the `./assume-role.sh` script, and leave out passing the `--profile` argument to CDK.


### Bootstrapping CDK

Bootstrapping only needs to be done when using CDK in an *environment* (account+region) for the first time.

See [the AWS docs](https://docs.aws.amazon.com/cdk/latest/guide/bootstrapping.html)
for more detail about the bootstrapping process.

The following command invokes the new (as at December 2020) "modern" boostrap template. 

*(You'll need to enter correct values for all the variables at the top.)*

```shell script
AWS_CLI_ADMIN_PROFILE_NAME=admin
ROOT_ACCOUNT_ID=1234567890
TARGET_ACCOUNT_ID=987654321
TARGET_REGION=ap-southeast-2
TARGET_ACCOUNT_ADMIN_ROLE=OrganizationAccountAccessRole

./assume-role.sh  "$AWS_CLI_ADMIN_PROFILE_NAME" \
  arn:aws:iam::"$TARGET_ACCOUNT_ID":role/"$TARGET_ACCOUNT_ADMIN_ROLE" \
  cdk-bootstrap

export CDK_NEW_BOOTSTRAP=1 \
    && cdk bootstrap \
    --profile cdk-bootstrap \
    --context bootstrap=true \
    --trust "$ROOT_ACCOUNT_ID" \
    --cloudformation-execution-policies arn:aws:iam::aws:policy/AdministratorAccess \
    aws://"$TARGET_ACCOUNT_ID"/"$REGION"
```


### Checking your CDK definitions

The following command will compile and run your Kotlin CDK definitions and attempt to synthesize them into a CloudFormation template for your environment.

(Replace `my-profile` with the name of your AWS CLI profile that is able to assume the `CdkDeploymentRole` in the target account/s.
If your default profile has this privilege, you should be able to simply run `cdk synth`.)  

```
cdk  --profile  my-profile  synth
```


### Deploying your CDK definitions

The following command will compile and run your Kotlin CDK definitions, attempt to synthesize them into a CloudFormation template for your environment, and then attempt to deploy the defined resources, or make other changes required to bring your environment into line with the template.

(Replace `my-profile` with the name of your AWS CLI profile that is able to assume the `CdkDeploymentRole` in the target account/s.
If your default profile has this privilege, you should be able to simply run `cdk synth`.
Replace `[stack_name]` with the name of the stack/ you want to deploy.
You can use wildcards, e.g. `Production-\*`.)  

```
cdk  --profile  cdk-build  deploy  [stack_name]
```


### Other useful commands

 * `cdk ls` - list all stacks
 * `cdk diff` - compare with current state
 * `cdk docs` - open CDK documentation
 * `cdk destroy [stack_name]` - destroy a deployed stack
 
 
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
