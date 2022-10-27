package fr.insee.kraftwerk.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Component
public class LogRequestFilter extends AbstractRequestLoggingFilter {

  private static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {
    String logRequest = this.getFormatLogRequest(request, message, getIdUser());
    log.info("START {}", logRequest);
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    String logRequest = this.getFormatLogRequest(request, message, getIdUser());
    log.info("END {}", logRequest);
  }

  private String getFormatLogRequest(HttpServletRequest request, String message, String idep) {
    StringBuilder sb =
        new StringBuilder("From ").append(request.getServerName()).append(" by user ").append(idep)
            .append(" call ").append(StringUtils.substringBetween(message, "[", "]"));
    if (StringUtils.isNotEmpty(request.getQueryString())) {
      sb.append(request.getQueryString());
    }
    return sb.toString();
  }

  private String getIdUser() {
		return "API";
	}

}
