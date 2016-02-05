package dk.kalhauge.thin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpRequestTest {
  private final String http =
      "GET /index.html HTTP/1.1\n"+
      "Content-Type: text/html\n"+
      "\n";
  
  @Test
  public void testGetMethod() throws IOException, Response.BadRequestException {
    InputStream in = new ByteArrayInputStream(http.getBytes());
    Request request = new HttpRequest(in);
    assertThat(request.getPath(), is("/index.html"));
    }
  
  @Test
  public void testGetPath() throws IOException, Response.BadRequestException {
    InputStream in = new ByteArrayInputStream(http.getBytes());
    Request request = new HttpRequest(in);
    assertThat(request.getPath(), is("/index.html"));
    }
  
  @Test
  public void testGetProtocol() throws IOException, Response.BadRequestException {
    InputStream in = new ByteArrayInputStream(http.getBytes());
    Request request = new HttpRequest(in);
    assertThat(request.getProtocol(), is("HTTP/1.1"));
    }
  
  @Test
  public void testGetContentType() throws IOException, Response.BadRequestException {
    InputStream in = new ByteArrayInputStream(http.getBytes());
    Request request = new HttpRequest(in);
    assertThat(request.getContentType(), is("text/html"));
    }
  
  }
