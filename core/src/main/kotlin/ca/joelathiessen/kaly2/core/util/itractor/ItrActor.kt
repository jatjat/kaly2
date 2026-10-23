package ca.joelathiessen.util.itractor

abstract class ItrActor(val inputChannel: ItrActorChannel) {

    // The implementing method should loop waiting for messages from the input channel until a StopMsg is received
    // For example see @see ca.joelathiessen.kaly2.core.tests.pc.unit.util.itractor.ItrActorSampleImpl#act
    abstract fun act()
}
