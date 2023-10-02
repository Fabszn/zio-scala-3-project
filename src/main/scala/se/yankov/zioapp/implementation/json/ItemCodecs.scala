package se.yankov.zioapp
package implementation
package json

import zio.json.*

import domain.ValidationStatus
import domain.item.{ CreateItemInput, UpdateItemInput }

object ItemCodecs:
  given JsonCodec[CreateItemInput[ValidationStatus.Unvalidated.type]] =
    DeriveJsonCodec.gen[CreateItemInput[ValidationStatus.Unvalidated.type]]
  given JsonCodec[UpdateItemInput[ValidationStatus.Unvalidated.type]] =
    DeriveJsonCodec.gen[UpdateItemInput[ValidationStatus.Unvalidated.type]]
