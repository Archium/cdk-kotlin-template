package com.example.cdk

import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Environment
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.core.Tags
import software.amazon.awscdk.services.ec2.SubnetConfiguration
import software.amazon.awscdk.services.ec2.SubnetType
import software.amazon.awscdk.services.ec2.Vpc

@Suppress("MemberVisibilityCanBePrivate")
class ExampleStack(parent: Construct?, id: String?, props: Props)
    : Stack(parent, id, StackProps.Builder().env(props.environment).build()) {

    init {
        Tags.of(this).apply {
            // TODO: Replace YOUR_ORG_NAME in this tag with your org name
            add("YOUR_ORG_NAME:cdk-source", this::class.simpleName!!)
        }
    }

    // TODO: Instantiate the resources that you want to be created in this Stack
    //  Here's an example that creates a VPC:
    val vpc: Vpc = vpc(this, "Vpc") {
        enableDnsSupport(true)
        subnetConfiguration(listOf(
            subnet { name("publicSubnet");  subnetType(SubnetType.PUBLIC) },
            subnet { name("privateSubnet"); subnetType(SubnetType.PRIVATE) }
        ))
    }

    class Props(val environment: Environment)
}

private fun vpc(scope: Construct, id: String, init: Vpc.Builder.() -> Unit) =
    Vpc.Builder.create(scope, id).build(init)

private fun subnet(init: SubnetConfiguration.Builder.() -> Unit) =
    SubnetConfiguration.builder().build(init)
