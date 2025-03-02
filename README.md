# **Sistema de Gerenciamento de Pedidos**

## **Visão Geral**

Este projeto é um *Sistema de Gerenciamento de Pedidos* desenvolvido em *Java com Spring Boot*. Ele permite o gerenciamento de pedidos, clientes e produtos, com integração a sistemas externos (*Produto Externo A* e *Produto Externo B*) para envio e recebimento de dados. O sistema é altamente escalável, suportando mais de **200 mil pedidos por dia**, garantindo **consistência** e **resiliência**.

---

## **Arquitetura**

O sistema é baseado no serviço *Order*, que processa pedidos e interage com os seguintes componentes:

- **Produto Externo A**: Envia pedidos ao sistema para processamento.
- **Serviço Order**: Processa pedidos, calcula valores totais e gerencia status, clientes e produtos.
- **Banco de Dados**: Armazena informações de pedidos, clientes e produtos.
- **Produto Externo B**: Recebe pedidos processados ao consultar o sistema.

---

## **Funcionalidades Principais**

- **Gerenciamento de Pedidos**: Criar, atualizar, consultar e excluir pedidos.
- **Gerenciamento de Clientes e Produtos**: Criar, atualizar, consultar e excluir informações de clientes e produtos.
- **Integração com Sistemas Externos**: Envio e recebimento de pedidos de plataformas externas.
- **Controle de Status**: Os pedidos só podem ser alterados enquanto estiverem no status *"CREATED"*.
- **Validações**: Garantia de unicidade de SKUs e prevenção de itens duplicados nos pedidos.

Consulte a documentação da API no *Swagger* para mais detalhes.

---

## **Tecnologias Utilizadas**

- **Java 21 ou superior**
- **Spring Boot 3**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **SLF4J**

---

## **Configuração e Instalação**

### **Pré-requisitos**

- **Java 21** ou superior instalado.
- **PostgreSQL** configurado.

### **Configuração do Banco de Dados**

1. Crie um banco de dados chamado `msorder` no PostgreSQL.
2. Configure as credenciais no arquivo `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/msorder
    username: seu_usuario
    password: sua_senha
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

3. Execute a aplicação com o comando:
   ```sh
   mvn spring-boot:run
   ```

---

## **Documentação da API**

A documentação completa da API está disponível no *Swagger*. Para acessá-la, utilize o seguinte link:

🔗 [Swagger UI](http://localhost:8080/swagger-ui.html)

---

## **Considerações Finais**

- **Escalabilidade**: Suporte para mais de **200 mil pedidos diários**.
- **Restrições de Negócio**: Os pedidos só podem ser modificados enquanto estiverem no status *"CREATED"*.
- **Validações**:
    - Unicidade de SKUs nos pedidos.
    - Prevenção de itens duplicados em um mesmo pedido.

Este sistema foi desenvolvido para garantir **eficiência, confiabilidade e alto desempenho** no processamento de pedidos. 🚀

