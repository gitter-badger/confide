package io.estatico.confide

import com.typesafe.config.{Config, ConfigObject}
import shapeless.{LabelledGeneric, Lazy}

trait FromConfObj[A] extends FromConf[A] {

  def decodeObject(o: ConfigObject): A

  override def get(config: Config, path: String): A = decodeObject(config.getObject(path))
}

object FromConfObj {

  def apply[A](implicit ev: FromConfObj[A]): FromConfObj[A] = ev

  def instance[A](f: ConfigObject => A): FromConfObj[A] = new FromConfObj[A] {
    override def decodeObject(o: ConfigObject): A = f(o)
  }

  /** Derive an instance for a case class. */
  def derive[A](implicit fc: Lazy[DerivedFromConfObj[A]]): FromConfObj[A] = fc.value
}

/**
 * Used internally to simplify calling `FromConf.derive` by only requiring a
 * single type param, inferring the HList representation.
 */
abstract class DerivedFromConfObj[A] extends FromConfObj[A]
object DerivedFromConfObj {
  implicit def derived[A, R](
    implicit
    g: LabelledGeneric.Aux[A, R],
    fc: Lazy[FromConfObj[R]]
  ): DerivedFromConfObj[A] = new DerivedFromConfObj[A] {
    override def decodeObject(o: ConfigObject): A = g.from(fc.value.decodeObject(o))
  }
}