/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author alexn_000
 */
public class AppendingObjectOutputStream extends ObjectOutputStream {

  public AppendingObjectOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  @Override
  protected void writeStreamHeader() throws IOException {
    // do not write a header, but reset:
    // this line added after another question
    // showed a problem with the original
    reset();
  }

}