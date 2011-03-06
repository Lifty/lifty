/*
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate

/**
 * Describes a variable binding that a Scalate template defines.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
case class Binding(name:String, className:String, importMembers:Boolean, defaultValue:Option[String], kind:String) {
  def this(name:String, className:String, importMembers:Boolean, defaultValue:Option[String]) =  this(name, className, importMembers, defaultValue, "val")
  def this(name:String, className:String, importMembers:Boolean) =  this(name, className, importMembers, None , "val")
  def this(name:String, className:String) =  this(name, className, false)
  def this(name:String) =  this(name, "Any") 
}