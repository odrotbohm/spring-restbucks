package de.odrotbohm.restbucks.core.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
class DocsController {

	@GetMapping(path = "/docs/{rel:^\\w+$}", produces = MediaType.TEXT_HTML_VALUE)
	String resolveDocs(@PathVariable String rel) {
		return "redirect:index.html#".concat(rel);
	}
}
