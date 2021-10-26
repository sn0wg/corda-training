- run uploadAttachment jar: /home/guilherme/Desktop/cachorro.zip
- start br.com.seven.training.flows.item.ItemCreationFlow$Initiator hash: 099D2317808E82F9C55169F5D0CF276821C600F23F4C4B64735B36CADA69D7A0
- start br.com.seven.training.flows.sevencoin.EnrollSevenCoin$Initiator value: 500, wallet: "O=PartyB,L=New York,C=US"
  start br.com.seven.training.flows.sevencoin.EnrollSevenCoin$Initiator value: 500, wallet: "O=PartyA,L=London,C=GB"
- start br.com.seven.training.flows.proposal.ProposalCreationFlow$Initiator hash: 099D2317808E82F9C55169F5D0CF276821C600F23F4C4B64735B36CADA69D7A0, value: 225
- start br.com.seven.training.flows.proposal.ProposalResponseFlow$Initiator response: ACCEPT, proposalID:
  start br.com.seven.training.flows.proposal.ProposalResponseFlow$Initiator response: REFUSE, proposalID:
- start br.com.seven.training.flows.proposal.ProposalStatePayFlow$Initiator proposalID: 

- run vaultQuery contractStateType: br.com.seven.training.state.ItemState
- run vaultQuery contractStateType: br.com.seven.training.state.ItemOwnershipState
- run vaultQuery contractStateType: br.com.seven.training.state.ProposalState
- run vaultQuery contractStateType: br.com.seven.training.state.SevenCoinState

- run openAttachment id: 099D2317808E82F9C55169F5D0CF276821C600F23F4C4B64735B36CADA69D7A0