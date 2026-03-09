# API de Microsserviços: Avaliador de Crédito e Mensageria

<p align="left">
  Este projeto implementa um ecossistema de microsserviços para avaliação de crédito e emissão de cartões, utilizando <strong>Spring Cloud</strong>, comunicação síncrona (<strong>OpenFeign</strong>) e assíncrona (<strong>RabbitMQ</strong>).
</p>

---

## 📊 Arquitetura e Fluxo

O diagrama abaixo ilustra a interação entre os serviços.
O **msavaliadorcredito** atua como **orquestrador**, agregando dados de outros serviços e solicitando processamentos assíncronos.

<img width="2816" height="1536" alt="Gemini_Generated_Image_yzyoa0yzyoa0yzyo" src="https://github.com/user-attachments/assets/d9aa8c28-8212-4796-99ca-8ecc0caa56af" />


> Observação: para desenvolvedores que preferem a versão Mermaid editável, mantenho a notação Mermaid no README anterior — mas a imagem SVG acima garante compatibilidade de visualização em todos os viewers.

---

## 🚀 Serviços do Ecossistema

### 🔍 Eureka Server (`eurekaserver`)

Responsável pelo **Service Discovery**.
Todos os microsserviços se registram aqui para permitir comunicação dinâmica sem URLs fixas.

* **Porta:** `8761`

---

### ☁️ Cloud Gateway (`mscloudgateway`)

API Gateway responsável por centralizar o acesso externo e rotear as requisições.

* **Porta:** `8080`
* **Rotas:**

  * `/clientes/**` → `msclientes`
  * `/cartoes/**` → `mscartoes`
  * `/avaliacoes-credito/**` → `msavaliadorcredito`

---

### 👤 MS Clientes (`msclientes`)

Gerencia os dados cadastrais dos clientes.

* **Tecnologias:** Spring Data JPA, H2
* **Dados:** Nome, CPF, Idade

**Endpoints:**

* `POST /clientes` → Cadastra cliente
* `GET /clientes?cpf={cpf}` → Consulta cliente por CPF

---

### 💳 MS Cartões (`mscartoes`)

Responsável por gerenciar tipos de cartões e cartões emitidos.
Também atua como **consumer** da fila RabbitMQ.

**Endpoints:**

* `POST /cartoes` → Cadastra tipo de cartão
* `GET /cartoes?renda={renda}` → Cartões disponíveis por renda
* `GET /cartoes?cpf={cpf}` → Cartões associados ao cliente

---

### 📊 MS Avaliador de Crédito (`msavaliadorcredito`)

Microsserviço **orquestrador**, responsável por integrar os demais serviços.

* Comunicação síncrona via **OpenFeign**
* Comunicação assíncrona via **RabbitMQ**

**Fluxos principais:**

* **Situação do Cliente:** Agrega dados do MS Clientes e MS Cartões
* **Avaliação de Crédito:** Calcula cartões aprovados com base em renda e idade
* **Emissão de Cartão:** Publica mensagem na fila `emissao-cartoes`

---

## 🛠️ Stack Tecnológico

<p align="center">
  <img src="https://img.shields.io/badge/Java-11-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-2.6.x-green" />
  <img src="https://img.shields.io/badge/Spring%20Cloud-2021.0.1-blue" />
  <img src="https://img.shields.io/badge/RabbitMQ-Mensageria-orange" />
  <img src="https://img.shields.io/badge/Docker-Container-blue" />
  <img src="https://img.shields.io/badge/Keycloak-OpenID%20Connect-8A2BE2" />
</p>

---

## ⚙️ Configuração de Mensageria

O sistema depende da seguinte fila no RabbitMQ, configurada no arquivo `MQConfig.java`:

```text
emissao-cartoes
```

---

## 🔐 Autenticação e Autorização (Keycloak)

Este projeto agora inclui autenticação e autorização baseada em OpenID Connect usando **Keycloak** (perfil de desenvolvimento). O Keycloak fornece um servidor de identidade para gerenciar realms, clientes e usuários. Abaixo estão instruções rápidas para rodar um Keycloak em modo de desenvolvimento e integrar localmente.

Observação: suponho uma configuração de desenvolvimento local — Keycloak em um container acessível em `http://localhost:8180`. Se preferir outra porta ou uma instalação gerenciada, adapte os comandos abaixo.

### Rodar o Keycloak (modo dev)

Execute o container do Keycloak (porta 8180 local para evitar conflito com o Gateway que usa 8080):

```bash
docker run -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

- Console de administração: http://localhost:8180/
- Usuário/Senha (dev): `admin` / `admin` (conforme variáveis acima)

### Configuração mínima recomendada (manual via console Keycloak)

1. Criar um Realm (ex.: `ms-realm`).
2. Criar um Client (ex.: `ms-client`) com Access Type `public` (para chamadas front-end / simples) ou `confidential` (se usar client secret). Configure o `Valid Redirect URIs` se necessário.
3. Criar usuários de teste (ex.: `usuario`, senha `senha`) e atribuir roles conforme necessário.

Para um ambiente de produção, não use `start-dev` e configure certificados, HTTPS e políticas de segurança adequadas.

### Como usar (exemplo rápido)

Depois de criar um usuário `usuario` no realm `ms-realm` e o client `ms-client` (public), você pode obter um token com grant_type=password (apenas para testes/dev):

```bash
curl -s -X POST "http://localhost:8180/realms/ms-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=usuario" \
  -d "password=senha" \
  -d "grant_type=password" \
  -d "client_id=ms-client"
```

O retorno contém o `access_token`. Use-o nas requisições ao Gateway/serviços:

```
Authorization: Bearer <access_token>
```

### Integração com os microsserviços

- Configure as aplicações Spring Boot (Gateway e microsserviços) para validar tokens OIDC apontando para o `issuer-uri` do Keycloak (ex.: `http://localhost:8180/realms/ms-realm`).
- No `application.yml` de cada serviço, defina as propriedades de segurança OAuth2/OIDC conforme a necessidade (resource-server/jwk-set-uri ou issuer-uri) e proteja endpoints com roles.

Se quiser, posso: gerar exemplos de configurações `application.yml` para o `mscloudgateway` e para um dos microsserviços (`msavaliadorcredito`) mostrando as propriedades `spring.security.oauth2` necessárias.


## ▶️ Como Executar o Projeto

### 1️⃣ Subir o RabbitMQ

```bash
docker run -it --rm --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.9-management
```

* Console de administração: [http://localhost:15672](http://localhost:15672)

  * **Usuário:** guest
  * **Senha:** guest

---

### 2️⃣ Iniciar os serviços (ordem recomendada)

0. **Keycloak** (opcional para autenticação) → porta `8180` (recomendo subir antes do Gateway)
1. **Eureka Server** → porta `8761`
2. **Cloud Gateway** → porta `8080`
3. Microsserviços:

   * `msclientes`
   * `mscartoes`
   * `msavaliadorcredito`

---

## ✅ Observações Finais

* Arquitetura baseada em **microsserviços desacoplados**
* Uso de **Service Discovery**, **API Gateway** e **Mensageria**
* Ideal para estudos de **Spring Cloud**, **OpenFeign** e **RabbitMQ**

---

Se quiser, posso:

* Ajustar o README para **portfólio GitHub**
* Criar uma versão em **inglês**
* Adicionar seção de **exemplos de requisições (cURL / Postman)**
