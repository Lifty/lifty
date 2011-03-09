/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import org.fusesource.scalate._


/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class TemplateEngineTest extends FunSuite {

  val templateEngine = new TemplateEngine
  templateEngine.workingDirectoryRoot = new File(List("target","test-data","TemplateEngineTest").mkString(java.io.File.separator))

  test("parse attribute declaration") {
    info("ok")
//    var template = templateEngine.load("src/test/resource/simple.ssp")

//    val context = new TemplateContext
//    template.renderTemplate(context)

  }

}
