/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
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


package org.fusesource.scalate.util

import java.io._

object IOUtil
{

  def loadBinaryFile( path: File ): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val in = new FileInputStream( path )
    try {
      copy( in, baos )
    } finally {
      in.close
    }

    baos.toByteArray
  }


  def writeBinaryFile( path: File, contents: Array[Byte] ): Unit = {
    val out = new FileOutputStream( path )
    try {
      out.write( contents )
    } finally {
      out.close
    }
  }


  def copy( in: InputStream, out: OutputStream ): Long = {
    var bytesCopied: Long = 0
    val buffer = new Array[Byte]( 8192 )

    var bytes = in.read( buffer )
    while( bytes >= 0 ) {
      out.write( buffer, 0, bytes )
      bytesCopied += bytes
      bytes = in.read( buffer )
    }

    bytesCopied
  }


  def copy( in: Reader, out: Writer ): Long = {
    var charsCopied: Long = 0
    val buffer = new Array[Char]( 8192 )

    var chars = in.read( buffer )
    while( chars >= 0 ) {
      out.write( buffer, 0, chars )
      charsCopied += chars
      chars = in.read( buffer )
    }

    charsCopied
  }

}
