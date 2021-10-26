package br.com.seven.training.models

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class ProposalStatus {
    CREATED,
    ACCEPTED,
    REFUSED,
    PAID
}