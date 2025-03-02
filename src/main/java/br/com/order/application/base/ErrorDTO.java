package br.com.order.application.base;

import java.util.List;

public record ErrorDTO(String path, List<String> errors) {
}
