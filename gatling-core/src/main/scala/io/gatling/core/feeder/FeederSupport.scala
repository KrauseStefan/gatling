/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.feeder

import io.gatling.commons.validation._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.feeder.SeparatedValuesParser._
import io.gatling.core.json.JsonParsers
import io.gatling.core.util.Resource

trait FeederSupport {

  implicit def seq2FeederBuilder[T](data: IndexedSeq[Map[String, T]])(implicit configuration: GatlingConfiguration): SourceFeederBuilder[T] = SourceFeederBuilder(InMemoryFeederSource(data), configuration)
  implicit def array2FeederBuilder[T](data: Array[Map[String, T]])(implicit configuration: GatlingConfiguration): SourceFeederBuilder[T] = SourceFeederBuilder(InMemoryFeederSource(data), configuration)
  implicit def feeder2FeederBuilder(feeder: Feeder[Any]): FeederBuilder = () => feeder

  def csv(fileName: String, quoteChar: Char = DefaultQuoteChar, escapeChar: Char = DefaultEscapeChar)(implicit configuration: GatlingConfiguration): BatchableFeederBuilder[String] =
    separatedValues(fileName, CommaSeparator, quoteChar, escapeChar)
  def ssv(fileName: String, quoteChar: Char = DefaultQuoteChar, escapeChar: Char = DefaultEscapeChar)(implicit configuration: GatlingConfiguration): BatchableFeederBuilder[String] =
    separatedValues(fileName, SemicolonSeparator, quoteChar, escapeChar)
  def tsv(fileName: String, quoteChar: Char = DefaultQuoteChar, escapeChar: Char = DefaultEscapeChar)(implicit configuration: GatlingConfiguration): BatchableFeederBuilder[String] =
    separatedValues(fileName, TabulationSeparator, quoteChar, escapeChar)

  def separatedValues(fileName: String, separator: Char, quoteChar: Char = DefaultQuoteChar, escapeChar: Char = DefaultEscapeChar)(implicit configuration: GatlingConfiguration): BatchableFeederBuilder[String] =
    Resource.feeder(fileName) match {
      case Success(resource) => new SourceFeederBuilder[String](new SeparatedValuesFeederSource(resource, separator, quoteChar, escapeChar), configuration) with BatchableFeederBuilder[String]
      case Failure(message)  => throw new IllegalArgumentException(s"Could not locate feeder file: $message")
    }

  def jsonFile(fileName: String)(implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration): SourceFeederBuilder[Any] =
    Resource.feeder(fileName) match {
      case Success(resource) =>
        val data = new JsonFeederFileParser().parse(resource)
        SourceFeederBuilder(InMemoryFeederSource(data), configuration)

      case Failure(message) => throw new IllegalArgumentException(s"Could not locate feeder file: $message")
    }

  def jsonUrl(url: String)(implicit jsonParsers: JsonParsers, configuration: GatlingConfiguration): SourceFeederBuilder[Any] = {
    val data = new JsonFeederFileParser().url(url)
    SourceFeederBuilder(InMemoryFeederSource(data), configuration)
  }
}
