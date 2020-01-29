package info.stasha.proxywarrior;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request wrapper.
 *
 * @author stasha
 */
public class HttpServletRequestWrapperImpl extends HttpServletRequestWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestWrapperImpl.class);

    private ServletInputStream inputStream;

    public class ServletInputStreamImpl extends ServletInputStream {

        private InputStream is;

        public ServletInputStreamImpl(InputStream is) {
            this.is = is;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }

    }

    public HttpServletRequestWrapperImpl(HttpServletRequest request) {
        super(request);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = new ServletInputStreamImpl(inputStream);

        try {
            IOUtils.closeQuietly(super.getInputStream());
        } catch (IOException ex) {
            LOGGER.error("Failed to get inputstream");
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.inputStream == null ? super.getInputStream() : this.inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return this.inputStream == null ? super.getReader() : new BufferedReader(new InputStreamReader(inputStream));
    }

}
