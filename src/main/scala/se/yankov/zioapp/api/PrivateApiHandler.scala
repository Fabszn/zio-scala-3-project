package se.yankov.zioapp
package api

import zio.*

import api.PrivateApiHandler
import api.item.*
import domain.*
import domain.item.*
import implementation.auth.*

import java.util.UUID

final case class PrivateApiHandler(authService: AuthService, itemService: ItemService):

  def createItem(authHeader: Option[String], input: CreateItemInput[ValidationStatus.Unvalidated.type])
      : IO[AuthError | RepositoryError.DbEx | RepositoryError.Conflict | NonEmptyChunk[ItemValidationError], ItemResult] =
    for {
      _              <- authService.validateJwt(authHeader.getOrElse(""))
      validatedInput <- ZIO.fromEither(ItemValidator.validate(input))
      item           <- itemService.addItem(validatedInput)
    } yield ItemResult.fromDomain(item)

  def updateItem(authHeader: Option[String], id: String, input: UpdateItemInput[ValidationStatus.Unvalidated.type])
      : IO[AuthError | RequestError | RepositoryError.DbEx | RepositoryError.MissingEntity | NonEmptyChunk[ItemValidationError], ItemResult] =
    for {
      _              <- authService.validateJwt(authHeader.getOrElse(""))
      itemId         <- ZIO.attempt(UUID.fromString(id)).mapBoth(err => RequestError(Some(err.getMessage)), ItemId(_))
      validatedInput <- ZIO.fromEither(ItemValidator.validate(input))
      item           <- itemService.updateItem(itemId, validatedInput)
    } yield ItemResult.fromDomain(item)

  def deleteItem(authHeader: Option[String], id: String)
      : IO[AuthError | RequestError | RepositoryError.DbEx | RepositoryError.MissingEntity, Unit] =
    authService.validateJwt(authHeader.getOrElse("")) *>
      ZIO
        .attempt(UUID.fromString(id))
        .mapError(err => RequestError(Some(err.getMessage)))
        .flatMap(id => itemService.deleteItem(ItemId(id)))

  def getItem(authHeader: Option[String], id: String)
      : IO[AuthError | RepositoryError.DbEx | RepositoryError.MissingEntity | RequestError, ItemResult] =
    authService.validateJwt(authHeader.getOrElse("")) *>
      ZIO
        .attempt(UUID.fromString(id))
        .mapError(err => RequestError(Some(err.getMessage)))
        .flatMap(id => itemService.getItemById(ItemId(id)).map(ItemResult.fromDomain(_)))

object PrivateApiHandler:
  def layer: RLayer[AuthService & ItemService, PrivateApiHandler] = ZLayer.derive[PrivateApiHandler]
