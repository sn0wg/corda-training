package br.com.seven.training.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class ProposalResponse {
    ACCEPT,
    REFUSE
}