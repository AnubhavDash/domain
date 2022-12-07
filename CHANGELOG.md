# Changelog

## Release 1.2

The following functionalities and improvements are included in release 1.2

* Added caching to the encryption group serialization / deserialization.
* Added a new participant "CANTON" to the direct trust keystores.
* Implemented the getCorrectnessInformationSelections and getCorrectnessInformationVotingOptions methods.
* Streamlined the handling of timezones in databases.
* Updated dependencies and third-party libraries.

---

## Release 1.1

The following functionalities and improvements are included in release 1.1

* Ensure that the PrimesMappingTable does not contain more than OMEGA (1200 entries)
* Integrate the QuadraticResidueToWriteIn and IntegerToWriteIn in the ProcessPlaintexts algorithm
* Add the list of write-in options to the CombinedCorrectnessInformation object

---

## Release 1.0

The following functionalities and improvements are included in release 1.0

* Refactored the encryptionParametersPayload object.
* Added the key generation Schnorr proofs to the ElectionEventContextPayload.
* Removed the optional keyword from the VerifiableShuffle in the TallyComponentShufflePayload.
* Added the PrimesMappingTable.
* Removed unused classes after the removal of the old orchestrator.
* Updated third-party libraries.

---

## Release 0.15

The following functionalities and improvements are included in release 0.15

* Added new mixnet objects
* Added additional validations on domain objects
* Upgraded library to Java 17

---

## Release 0.14

The following functionalities and improvements are included in release 0.14

* Added classes to serialize ElGamalMultiRecipient key pairs.
* Improved the validateUUID method.

---

## Release 0.13

The following functionalities and improvements are included in release 0.13:

* Refactored the CombinedCorrectnessInformation object and GetEncodedVotingOptions method.
* Included the electionEventID and ballotBoxID in the Mix net payload objects.
