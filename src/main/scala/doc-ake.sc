import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider
import otr.Handler
import otr.handlers.ake.{DHCommitHandler, InitHandler}
import otr.messages.Empty

Security.addProvider(new BouncyCastleProvider())

var alice: Handler = InitHandler.create().toOption.get

var bob: Handler = DHCommitHandler.create().toOption.get

var res = alice.handle(Empty()).toOption.get

res.actions.head.getClass
alice = res.newHandler

// DHCommit -> DHKey
res = bob.handle(res.actions.head).toOption.get

res.actions.head.getClass
bob = res.newHandler

// DHKey
res = alice.handle(res.actions.head).toOption.get

res.actions.head.getClass
alice = res.newHandler

// RevealSignature
res = bob.handle(res.actions.head).toOption.get

res.actions.head.getClass
bob = res.newHandler

// Signature
res = alice.handle(res.actions.head).toOption.get

throw new Exception("asdf")


