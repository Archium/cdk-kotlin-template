package com.example.cdk

import com.example.cdk.stacks.ExampleStack
import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.Environment
import software.amazon.awscdk.core.Tags

class ExampleCdkApp : App() {

    init {
        Tags.of(this).apply {
            // TODO: Replace YOUR_ORG_NAME in these tags with your org name
            add("YOUR_ORG_NAME:managed-by", "CDK")
            add("YOUR_ORG_NAME:cdk-source", this::class.simpleName!!)
        }

        ExampleStack(this, "ExampleStack1", ExampleStack.Props(sydneyEnvironment))
    }

    companion object {

        // TODO: Change this to the AWS Account ID where you want to deploy resources
        private const val AWS_ACCOUNT_ID: String = "1234567890"

        // TODO: Change this to the AWS Region where you want to deploy resources
        private const val AWS_REGION_NAME: String = "ap-southeast-2"

        private val sydneyEnvironment: Environment = Environment.Builder().build {
            account(AWS_ACCOUNT_ID)
            region(AWS_REGION_NAME)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            ExampleCdkApp().synth()
        }
    }
}
