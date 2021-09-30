// package zio.schema

// import scala.collection.immutable.ListMap

// import zio.Chunk
// import zio.random.Random
// import zio.test.{ Gen, Sized }

// object SchemaGen {

//   def anyStructure(schemaGen: Gen[Random with Sized, Schema[_]]): Gen[Random with Sized, Seq[Schema.Field[_]]] =
//     Gen.setOfBounded(1, 30)(Gen.anyString.filter(_.isEmpty)).flatMap { keySet =>
//       Gen.setOfN(keySet.size)(schemaGen).map { schemas =>
//         keySet
//           .zip(schemas)
//           .map {
//             case (label, schema) => Schema.Field(label, schema)
//           }
//           .toSeq
//       }
//     }

//   def anyStructure[A](schema: Schema[A]): Gen[Random with Sized, Seq[Schema.Field[A]]] =
//     Gen
//       .setOfBounded(1, 30)(
//         Gen.anyString.map(Schema.Field(_, schema))
//       )
//       .map(_.toSeq)

//   def anyEnumeration(schemaGen: Gen[Random with Sized, Schema[_]]): Gen[Random with Sized, ListMap[String, Schema[_]]] =
//     Gen
//       .listOfBounded(1, 10)(
//         Gen.anyString.zip(schemaGen)
//       )
//       .map(ListMap.empty ++ _)

//   def anyEnumeration[A](schema: Schema[A]): Gen[Random with Sized, ListMap[String, Schema[A]]] =
//     Gen.listOfBounded(1, 10)(Gen.anyString.map(_ -> schema)).map(ListMap.empty ++ _)

//   val anyPrimitive: Gen[Random, Schema.Primitive[_]] =
//     StandardTypeGen.anyStandardType.map(Schema.Primitive(_))

//   type PrimitiveAndGen[A] = (Schema.Primitive[A], Gen[Random with Sized, A])

//   val anyPrimitiveAndGen: Gen[Random, PrimitiveAndGen[Any]] =
//     StandardTypeGen.anyStandardTypeAndGen.map {
//       case (standardType, gen) => Schema.Primitive(standardType.asInstanceOf[StandardType[Any]]) -> gen
//     }

//   type PrimitiveAndValue[A] = (Schema.Primitive[A], A)

//   val anyPrimitiveAndValue: Gen[Random with Sized, PrimitiveAndValue[Any]] =
//     for {
//       (schema, gen) <- anyPrimitiveAndGen
//       value         <- gen
//     } yield schema -> value

//   def anyOptional(schemaGen: Gen[Random with Sized, Schema[Any]]): Gen[Random with Sized, Schema.Optional[Any]] =
//     schemaGen.map(Schema.Optional(_))

//   type OptionalAndGen[A] = (Schema.Optional[A], Gen[Random with Sized, Option[A]])

//   val anyOptionalAndGen: Gen[Random with Sized, OptionalAndGen[Any]] =
//     anyPrimitiveAndGen.map {
//       case (schema, gen) => Schema.Optional(schema) -> Gen.option(gen)
//     }

//   type OptionalAndValue[A] = (Schema.Optional[A], Option[A])

//   val anyOptionalAndValue: Gen[Random with Sized, OptionalAndValue[Any]] =
//     for {
//       (schema, gen) <- anyOptionalAndGen
//       value         <- gen
//     } yield schema -> value

//   val anyEither: Gen[Random with Sized, Schema.EitherSchema[Any, Any]] =
//     for {
//       left  <- anyPrimitive
//       right <- anyPrimitive
//     } yield Schema.EitherSchema(left.asInstanceOf[Schema[Any]], right.asInstanceOf[Schema[Any]])

//   type EitherAndGen[A, B] = (Schema.EitherSchema[A, B], Gen[Random with Sized, Either[A, B]])

//   val anyEitherAndGen: Gen[Random with Sized, EitherAndGen[Any, Any]] =
//     for {
//       (leftSchema, leftGen)   <- anyPrimitiveAndGen
//       (rightSchema, rightGen) <- anyPrimitiveAndGen
//     } yield (Schema.EitherSchema(leftSchema, rightSchema), Gen.either(leftGen, rightGen))

//   type EitherAndValue[A, B] = (Schema.EitherSchema[A, B], Either[A, B])

//   val anyEitherAndValue: Gen[Random with Sized, EitherAndValue[Any, Any]] =
//     for {
//       (schema, gen) <- anyEitherAndGen
//       value         <- gen
//     } yield (schema, value)

//   lazy val anyTuple: Gen[Random with Sized, Schema.Tuple[_, _]] =
//     anySchema.zipWith(anySchema) { (a, b) =>
//       Schema.Tuple(a, b)
//     }

//   type TupleAndGen[A, B] = (Schema.Tuple[A, B], Gen[Random with Sized, (A, B)])

//   val anyTupleAndGen: Gen[Random with Sized, TupleAndGen[Any, Any]] =
//     for {
//       (schemaA, genA) <- anyPrimitiveAndGen
//       (schemaB, genB) <- anyPrimitiveAndGen
//     } yield Schema.Tuple(schemaA, schemaB) -> genA.zip(genB)

//   type TupleAndValue[A, B] = (Schema.Tuple[A, B], (A, B))

//   val anyTupleAndValue: Gen[Random with Sized, TupleAndValue[Any, Any]] =
//     for {
//       (schema, gen)    <- anyTupleAndGen
//       (valueA, valueB) <- gen
//     } yield schema -> ((valueA, valueB))

//   val anySequence: Gen[Random with Sized, Schema[Chunk[Any]]] =
//     anySchema.map(Schema.chunk(_).asInstanceOf[Schema[Chunk[Any]]])

//   type SequenceAndGen[A] = (Schema[Chunk[A]], Gen[Random with Sized, Chunk[A]])

//   val anySequenceAndGen: Gen[Random with Sized, SequenceAndGen[Any]] =
//     anyPrimitiveAndGen.map {
//       case (schema, gen) =>
//         Schema.chunk(schema.asInstanceOf[Schema[Any]]) -> Gen.chunkOf(gen)
//     }

//   type SequenceAndValue[A] = (Schema[Chunk[A]], Chunk[A])

//   val anySequenceAndValue: Gen[Random with Sized, SequenceAndValue[Any]] =
//     for {
//       (schema, gen) <- anySequenceAndGen
//       value         <- gen
//     } yield schema -> value

//   val anyEnumeration: Gen[Random with Sized, Schema[(String, Any)]] =
//     anyEnumeration(anySchema).map(Schema.enumeration)

//   type EnumerationAndGen = (Schema[(String, _)], Gen[Random with Sized, (String, _)])

//   val anyEnumerationAndGen: Gen[Random with Sized, EnumerationAndGen] =
//     for {
//       primitiveAndGen <- anyPrimitiveAndGen
//       structure       <- anyEnumeration(primitiveAndGen._1)
//       primitiveValue  <- primitiveAndGen._2
//     } yield {
//       val gen = Gen.oneOf(structure.keys.map(Gen.const(_)).toSeq: _*).map(l => l -> primitiveValue)
//       Schema.enumeration(structure) -> gen
//     }

//   type EnumerationAndValue = (Schema[(String, Any)], (String, Any))

//   val anyEnumerationAndValue: Gen[Random with Sized, EnumerationAndValue] =
//     for {
//       (schema, gen) <- anyEnumerationAndGen
//       value         <- gen
//     } yield schema -> value

//   val anyRecord: Gen[Random with Sized, Schema[ListMap[String, Any]]] =
//     anyStructure(anySchema).map(Schema.record)

//   type GenericRecordAndGen = (Schema[ListMap[String, Any]], Gen[Random with Sized, ListMap[String, Any]])

//   val anyGenericRecordAndGen: Gen[Random with Sized, GenericRecordAndGen] =
//     for {
//       (schema, gen) <- anyPrimitiveAndGen
//       structure     <- anyStructure(schema)
//     } yield {
//       val valueGen = Gen
//         .const(structure.map(_.label))
//         .zip(Gen.listOfN(structure.size)(gen))
//         .map {
//           case (labels, values) =>
//             labels.zip(values)
//         }
//         .map(ListMap.empty ++ _)

//       Schema.record(structure: _*) -> valueGen
//     }

//   type RecordAndValue = (Schema[ListMap[String, Any]], ListMap[String, Any])

//   val anyRecordAndValue: Gen[Random with Sized, RecordAndValue] =
//     for {
//       (schema, gen) <- anyGenericRecordAndGen
//       value         <- gen
//     } yield schema -> value

//   val anyRecordOfRecordsAndValue: Gen[Random with Sized, RecordAndValue] =
//     for {
//       (schema1, gen1) <- anyGenericRecordAndGen
//       (schema2, gen2) <- anyGenericRecordAndGen
//       (schema3, gen3) <- anyGenericRecordAndGen
//       keys            <- Gen.listOfN(3)(Gen.anyString.filter(_.length() > 0))
//       (key1, value1)  <- Gen.const(keys(0)).zip(gen1)
//       (key2, value2)  <- Gen.const(keys(1)).zip(gen2)
//       (key3, value3)  <- Gen.const(keys(2)).zip(gen3)
//     } yield Schema.record(Schema.Field(key1, schema1), Schema.Field(key2, schema2), Schema.Field(key3, schema3)) -> ListMap(
//       (key1, value1),
//       (key2, value2),
//       (key3, value3)
//     )

//   type SequenceTransform[A] = Schema.Transform[Chunk[A], List[A]]

//   val anySequenceTransform: Gen[Random with Sized, SequenceTransform[Any]] = {
//     anySequence.map(schema => transformSequence(schema))
//   }

//   type SequenceTransformAndGen[A] = (SequenceTransform[A], Gen[Random with Sized, List[A]])

//   val anySequenceTransformAndGen: Gen[Random with Sized, SequenceTransformAndGen[Any]] =
//     anyPrimitiveAndGen.map {
//       case (schema, gen) =>
//         transformSequence(Schema.chunk(schema.asInstanceOf[Schema[Any]])) -> Gen.listOf(gen)
//     }

//   // TODO: Add some random Left values.
//   private def transformSequence[A](schema: Schema[Chunk[A]]): SequenceTransform[A] =
//     Schema.Transform[Chunk[A], List[A]](schema, chunk => Right(chunk.toList), list => Right(Chunk.fromIterable(list)))

//   type SequenceTransformAndValue[A] = (SequenceTransform[A], List[A])

//   val anySequenceTransformAndValue: Gen[Random with Sized, SequenceTransformAndValue[Any]] =
//     for {
//       (schema, gen) <- anySequenceTransformAndGen
//       value         <- gen
//     } yield schema -> value

//   type RecordTransform[A] = Schema.Transform[ListMap[String, Any], A]

//   val anyRecordTransform: Gen[Random with Sized, RecordTransform[Any]] = {
//     anyRecord.map(schema => transformRecord(schema))
//   }

//   type RecordTransformAndGen[A] = (RecordTransform[A], Gen[Random with Sized, A])

//   // TODO: How do we generate a value of a type that we know nothing about?
//   val anyRecordTransformAndGen: Gen[Random with Sized, RecordTransformAndGen[Any]] =
//     Gen.empty
//   //    anyRecordAndGen.map {
//   //      case (schema, gen) => transformRecord(schema) -> gen
//   //    }

//   // TODO: Dynamically generate a case class.
//   def transformRecord[A](schema: Schema[ListMap[String, Any]]): RecordTransform[A] =
//     Schema.Transform[ListMap[String, Any], A](schema, _ => Left("Not implemented."), _ => Left("Not implemented."))

//   type RecordTransformAndValue[A] = (RecordTransform[A], A)

//   val anyRecordTransformAndValue: Gen[Random with Sized, RecordTransformAndValue[Any]] =
//     for {
//       (schema, gen) <- anyRecordTransformAndGen
//       value         <- gen
//     } yield schema -> value

//   type EnumerationTransform[A] = Schema.Transform[(String, _), A]

//   val anyEnumerationTransform: Gen[Random with Sized, EnumerationTransform[Any]] = {
//     anyEnumeration.map(schema => transformEnumeration(schema.asInstanceOf[Schema[(String, Any)]]).asInstanceOf[Schema.Transform[(String, Any), Any]])
//   }

//   type EnumerationTransformAndGen[A] = (EnumerationTransform[A], Gen[Random with Sized, A])

//   // TODO: How do we generate a value of a type that we know nothing about?
//   val anyEnumerationTransformAndGen: Gen[Random with Sized, EnumerationTransformAndGen[Any]] =
//     Gen.empty
//   //    anyEnumerationAndGen.map {
//   //      case (schema, gen) => transformEnumeration(schema) -> gen
//   //    }

//   // TODO: Dynamically generate a sealed trait and case/value classes.
//   def transformEnumeration[A](schema: Schema[(String, _)]): EnumerationTransform[_] =
//     Schema.Transform[(String, _), A](schema, _ => Left("Not implemented."), _ => Left("Not implemented."))

//   type EnumerationTransformAndValue[A] = (EnumerationTransform[A], A)

//   val anyEnumerationTransformAndValue: Gen[Random with Sized, EnumerationTransformAndValue[Any]] =
//     for {
//       (schema, gen) <- anyEnumerationTransformAndGen
//       value         <- gen
//     } yield schema -> value

//   val anyTransform: Gen[Random with Sized, Schema.Transform[Any, Any]] = Gen.oneOf(
//     anySequenceTransform.asInstanceOf[Gen[Random with Sized, Schema.Transform[Any, Any]]],
//     anyRecordTransform.asInstanceOf[Gen[Random with Sized, Schema.Transform[Any, Any]]],
//     anyEnumerationTransform.asInstanceOf[Gen[Random with Sized, Schema.Transform[Any, Any]]]
//   )

//   type TransformAndValue[A] = (Schema.Transform[Any, A], A)

//   val anyTransformAndValue: Gen[Random with Sized, TransformAndValue[Any]] =
//     Gen.oneOf[Random with Sized, TransformAndValue[Any]](
//       anySequenceTransformAndValue.asInstanceOf[Gen[Random with Sized, TransformAndValue[Any]]]
//       // anyRecordTransformAndValue,
//       // anyEnumerationTransformAndValue
//     )

//   type TransformAndGen[A] = (Schema.Transform[Any, A], Gen[Random with Sized, A])

//   val anyTransformAndGen: Gen[Random with Sized, TransformAndGen[Any]] =
//     Gen.oneOf[Random with Sized, TransformAndGen[Any]](
//       anySequenceTransformAndGen.asInstanceOf[Gen[Random with Sized, TransformAndGen[Any]]],
//       anyRecordTransformAndGen.asInstanceOf[Gen[Random with Sized, TransformAndGen[Any]]],
//       anyEnumerationTransformAndGen.asInstanceOf[Gen[Random with Sized, TransformAndGen[Any]]]
//     )

//   lazy val anySchema: Gen[Random with Sized, Schema[Any]] =
//     for {
//       treeDepth <- Gen.bounded(0, 2)(Gen.const(_))
//       tree      <- anyTree(treeDepth)
//     } yield tree.asInstanceOf[Schema[Any]]

//   def anyValueForSchema[A](schema: Schema[A]): Gen[Random with Sized, (Schema[A], A)] =
//     DynamicValueGen
//       .anyDynamicValueOfSchema(schema)
//       .map { dynamic =>
//         schema -> schema.fromDynamic(dynamic).toOption.get
//       }

//   type SchemaAndValue[A] = (Schema[A], A)

//   lazy val anySchemaAndValue: Gen[Random with Sized, SchemaAndValue[Any]] =
//     for {
//       schema  <- anySchema
//       dynamic <- DynamicValueGen.anyDynamicValueOfSchema(schema)
//     } yield (schema -> schema.fromDynamic(dynamic).toOption.get).asInstanceOf[SchemaAndValue[Any]]

//   sealed trait Arity
//   case object Arity0                  extends Arity
//   final case class Arity1(value: Int) extends Arity

//   object Arity1 {
//     implicit val schema: Schema[Arity1] = ???//DeriveSchema.gen[Arity1]
//   }
//   final case class Arity2(value1: String, value2: Arity1) extends Arity

//   object Arity2 {
//     implicit val schema: Schema[Arity2] = ???// DeriveSchema.gen[Arity2]
//   }
//   final case class Arity3(value1: String, value2: Arity2, value3: Arity1) extends Arity

//   object Arity3 {
//     implicit val schema: Schema[Arity3] = ???//DeriveSchema.gen[Arity3]
//   }
//   final case class Arity24(
//     a1: Arity1,
//     a2: Arity2,
//     a3: Arity3,
//     f4: Int = 4,
//     f5: Int = 5,
//     f6: Int = 6,
//     f7: Int = 7,
//     f8: Int = 8,
//     f9: Int = 9,
//     f10: Int = 10,
//     f11: Int = 11,
//     f12: Int = 12,
//     f13: Int = 13,
//     f14: Int = 14,
//     f15: Int = 15,
//     f16: Int = 16,
//     f17: Int = 17,
//     f18: Int = 18,
//     f19: Int = 19,
//     f20: Int = 20,
//     f21: Int = 21,
//     f22: Int = 22,
//     f23: Int = 23,
//     f24: Int = 24
//   ) extends Arity

//   object Arity24 {
//     implicit val schema: Schema[Arity24] = ???//DeriveSchema.gen[Arity24]
//   }

//   object Arity {
//     implicit val arityEnumSchema: Schema[Arity] = ???//DeriveSchema.gen[Arity]
//   }

//   lazy val anyArity1: Gen[Random with Sized, Arity1] = Gen.anyInt.map(Arity1(_))

//   lazy val anyArity2: Gen[Random with Sized, Arity2] =
//     for {
//       s  <- Gen.anyString
//       a1 <- anyArity1
//     } yield Arity2(s, a1)

//   lazy val anyArity3: Gen[Random with Sized, Arity3] =
//     for {
//       s  <- Gen.anyString
//       a1 <- anyArity1
//       a2 <- anyArity2
//     } yield Arity3(s, a2, a1)

//   lazy val anyArity24: Gen[Random with Sized, Arity24] =
//     for {
//       a1 <- anyArity1
//       a2 <- anyArity2
//       a3 <- anyArity3
//     } yield Arity24(a1, a2, a3)

//   lazy val anyArity: Gen[Random with Sized, Arity] = Gen.oneOf(anyArity1, anyArity2, anyArity3, anyArity24)

//   type CaseClassAndGen[A] = (Schema[A], Gen[Sized with Random, A])

//   type CaseClassAndValue[A] = (Schema[A], A)

//   lazy val anyCaseClassSchema: Gen[Random with Sized, Schema[_]] =
//     Gen.oneOf(
//       Gen.const(Schema[Arity1]),
//       Gen.const(Schema[Arity2]),
//       Gen.const(Schema[Arity3]),
//       Gen.const(Schema[Arity24])
//     )

//   val anyCaseClassAndGen: Gen[Random with Sized, CaseClassAndGen[Any]] =
//     anyCaseClassSchema.map {
//       case s @ Schema.CaseClass1(_, _, _, _)             => (s -> anyArity1).asInstanceOf[CaseClassAndGen[Any]]
//       case s @ Schema.CaseClass2(_, _, _, _, _, _)       => (s -> anyArity2).asInstanceOf[CaseClassAndGen[Any]]
//       case s @ Schema.CaseClass3(_, _, _, _, _, _, _, _) => (s -> anyArity3).asInstanceOf[CaseClassAndGen[Any]]
//       case s                                             => (s -> anyArity24).asInstanceOf[CaseClassAndGen[Any]]
//     }

//   val anyCaseClassAndValue: Gen[Random with Sized, CaseClassAndValue[Any]] =
//     for {
//       (schema, gen) <- anyCaseClassAndGen
//       value         <- gen
//     } yield (schema -> value)

//   type EnumAndGen[A] = (Schema[A], Gen[Random with Sized, A])

//   type EnumAndValue[A] = (Schema[A], A)

//   lazy val anyEnumSchema: Gen[Any, Schema[Arity]] = Gen.const(Arity.arityEnumSchema)

//   val anyEnumAndGen: Gen[Random with Sized, EnumAndGen[Any]] =
//     anyEnumSchema.map(_ -> anyArity).asInstanceOf[Gen[Random with Sized, EnumAndGen[Any]]]

//   val anyEnumAndValue: Gen[Random with Sized, EnumAndValue[Any]] =
//     for {
//       (schema, gen) <- anyEnumAndGen
//       value         <- gen
//     } yield schema -> value

//   lazy val anyLeaf: Gen[Random with Sized, Schema[Any]] =
//     Gen.oneOf(
//       anyPrimitive.asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyPrimitive.map(Schema.list(_)).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyPrimitive.map(_.optional).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       // anyPrimitive.zip(anyPrimitive).map { case (l, r) => Schema.either(l, r) },
//       anyPrimitive.zip(anyPrimitive).map { case (l, r) => Schema.tuple2(l, r) }.asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyStructure(anyPrimitive).map(fields => Schema.GenericRecord(Chunk.fromIterable(fields))).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyEnumeration(anyPrimitive).map(Schema.enumeration(_)).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyCaseClassSchema.asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//       anyEnumSchema.asInstanceOf[Gen[Random with Sized, Schema[Any]]]
//     )

//   def anyTree(depth: Int): Gen[Random with Sized, Schema[Any]] =
//     if (depth == 0)
//       anyLeaf
//     else
//       Gen.oneOf(
//         anyTree(depth - 1).map(Schema.list(_)).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//         anyTree(depth - 1).map(_.optional).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//         anyTree(depth - 1).zip(anyTree(depth - 1)).map { case (l, r) => Schema.either(l, r) }.asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//         anyTree(depth - 1).zip(anyTree(depth - 1)).map { case (l, r) => Schema.tuple2(l, r) }.asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//         anyStructure(anyTree(depth - 1)).map(fields => Schema.GenericRecord(Chunk.fromIterable(fields))).asInstanceOf[Gen[Random with Sized, Schema[Any]]],
//         anyEnumeration(anyTree(depth - 1)).map(Schema.enumeration(_)).asInstanceOf[Gen[Random with Sized, Schema[Any]]]
//       )

//   type SchemaAndDerivedValue[A] = (Schema[A], Either[String, A])

//   lazy val anyLeafAndValue: Gen[Random with Sized, SchemaAndValue[Any]] =
//     for {
//       schema <- anyLeaf
//       value  <- DynamicValueGen.anyDynamicValueOfSchema(schema)
//     } yield (schema -> schema.fromDynamic(value).toOption.get).asInstanceOf[SchemaAndValue[Any]]

//   lazy val anyTreeAndValue: Gen[Random with Sized, SchemaAndValue[Any]] =
//     for {
//       schema <- anyTree(1)
//       value  <- DynamicValueGen.anyDynamicValueOfSchema(schema)
//     } yield (schema -> schema.fromDynamic(value).toOption.get).asInstanceOf[SchemaAndValue[Any]]

//   sealed trait Json
//   case object JNull                                extends Json
//   case class JString(s: String)                    extends Json
//   case class JNumber(l: Int)                       extends Json
//   case class JDecimal(d: Double)                   extends Json
//   case class JObject(fields: List[(String, Json)]) extends Json
//   case class JArray(fields: List[Json])            extends Json

//   object Json {
//     implicit lazy val schema: Schema[Json] = ???//DeriveSchema.gen[Json]

//     val leafGen: Gen[Random with Sized, Json] =
//       Gen.oneOf(
//         Gen.const(JNull),
//         Gen.anyString.map(JString(_)),
//         Gen.anyInt.map(JNumber(_))
//       )

//     val gen: Gen[Random with Sized, Json] =
//       for {
//         keys   <- Gen.setOfN(3)(Gen.anyString)
//         values <- Gen.setOfN(3)(leafGen)
//       } yield JObject(keys.zip(values).toList)
//   }

//   lazy val anyRecursiveType: Gen[Random with Sized, Schema[Any]] =
//     Gen.const(Schema[Json]).asInstanceOf[Gen[Random with Sized, Schema[Any]]]

//   lazy val anyRecursiveTypeAndValue: Gen[Random with Sized, SchemaAndValue[Any]] =
//     for {
//       schema <- Gen.const(Schema[Json])
//       value  <- Json.gen
//     } yield (schema.asInstanceOf[Schema[Any]], value)

// }
