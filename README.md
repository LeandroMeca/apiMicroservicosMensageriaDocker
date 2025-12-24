<h1 align="center">API de Microsserviços: Avaliador de Crédito e Mensageria</h1>

<p align="center">
  Este projeto implementa um ecossistema de microsserviços para avaliação de crédito e emissão de cartões, utilizando <strong>Spring Cloud</strong>, comunicação síncrona (<strong>OpenFeign</strong>) e assíncrona (<strong>RabbitMQ</strong>).
</p>

<hr />

<h2>📊 Arquitetura e Fluxo Gráfico</h2>

<p>
  O diagrama abaixo ilustra a interação entre os serviços. O <strong>msavaliadorcredito</strong> atua como orquestrador, agregando dados de outros serviços e solicitando processamentos assíncronos.
</p>

<div align="center">
  <img src="https://mermaid.ink/img/pako:eNqdVU1v2zAM_SuCzmkD-7F7K7BFuw3rYR2K7bQcg8GWYxO2JdOk4qBB__uocZymWbsN22GQRT5-fCTF54yKVhEJaW9f86p9Y1S0T8mC3Zf7A_vG2ZfDb8-Hw988-8255R_WLT99_i04a2Wc99bCO-f_m7vj8eT-9uF-c2_58f6r5afD_tB-2e92h83t5uHu4JbS3yQW-Jd1y1L0W3683f2y_HRz_7B5gM_3d5uXg2zE01QpP5eU1a3UEn7_VjFOS2W0BqVw4q2T2lqF01g7Y7WyFqWv5k7D30oG3C_21vI752E-9-2Bv9_z8xN0C1fALyA_w3fQ17D0_Fdw5y1fgl-530Afw204WssX4Ffuz_wZ0B18CX4FfcX-zJ_B17ACfwZ9x5_5M9CG_hn4M-gH_syfwXfwJfgV9DP7C38G38AK_Bn0g_2FP4O_wZfgz6Bf2F_4M7iBL8GvoF_ZX_gz2IbT0Fv-DPqN_YU_w_7gy_H1l5_5V6ANXf8V9Cv7G3-GfeAr8GfQb-xv_BnuA1-BP4N_4M_8GdwFvgJ_Bv3O_safwT7wFfgz6A_2N_4MdTiF0Vo-4v8E2vA3fAX-DPqT_Y0_w33gK_Bn8C_8mT-Du8BX4M-gP9nf-DPYB74Cfwb9yf7Gn8E2dANX4M-gP9nf-DPUB1-BP4P-bH_jz3Af-Ar8Gfwn_syfwV3gK_Bn0F_sb_wZ7ANfgT-D_mJ_48_wPvgK_Bn0F_sbf4b64CvwZ9Bf7G_8Ge4DX4E_g__Cn_kzuAt8Bf4M-ov9jT-DfSC4f8Q_gX5lf-PPsA98Bf4M-o39jT_DfeAr8GfwD_yZP4O7wFfgz6Df2d_4M9gHvgJ_Bv3B_safYT-cRqflI_5P4M_8GdwFvgJ_Bv3J_safwT7wFfgz6E_2N_4MtqEbuAJ_Bv3J_saf4T7wFfgz6E_2N_4M94GvwJ_Bf-LP_BncBb4Cfwb9xf7Gn8E-8BX4M-gv9jf-DO-Dr8CfQX-xv_FnqA--An8G_cX_xl8-06_U0_S_kO59_pC00i-kG1L71lqjP9O9tXBKs5KyXkK9N5q0s9bX2oT0d8-04b12WqekX0e9Nf8Ac1C81A" alt="Diagrama de Arquitetura em Mermaid" />
</div>

<br />

<h2>🚀 Serviços do Ecossistema</h2>

<ul>
  <li>
    <strong>🔍 Eureka Server (<code>eurekaserver</code>)</strong><br />
    Responsável pelo <em>Service Discovery</em>. Todos os microsserviços se registram aqui na porta <code>8761</code> para comunicação dinâmica sem URLs fixas.
  </li>
  
  <li>
    <strong>☁️ Cloud Gateway (<code>mscloudgateway</code>)</strong><br />
    API Gateway rodando na porta <code>8080</code>. Centraliza a entrada e roteia as requisições:
    <ul>
      <li><code>/clientes/**</code> → <em>msclientes</em></li>
      <li><code>/cartoes/**</code> → <em>mscartoes</em></li>
      <li><code>/avaliacoes-credito/**</code> → <em>msavaliadorcredito</em></li>
    </ul>
  </li>

  <li>
    <strong>👤 MS Clientes (<code>msclientes</code>)</strong><br />
    Gerencia dados cadastrais (Nome, CPF, Idade). Utiliza <strong>Spring Data JPA</strong> e banco <strong>H2</strong>.
    <br /><em>Endpoints:</em>
    <ul>
      <li><code>POST /clientes</code>: Cadastra cliente.</li>
      <li><code>GET /clientes?cpf={cpf}</code>: Busca dados do cliente.</li>
    </ul>
  </li>

  <li>
    <strong>💳 MS Cartões (<code>mscartoes</code>)</strong><br />
    Gerencia tipos de cartões e cartões emitidos. Atua como <em>subscriber</em> de filas RabbitMQ.
    <br /><em>Endpoints:</em>
    <ul>
      <li><code>POST /cartoes</code>: Cadastra tipo de cartão.</li>
      <li><code>GET /cartoes?renda={renda}</code>: Cartões disponíveis por renda.</li>
      <li><code>GET /cartoes?cpf={cpf}</code>: Cartões do cliente.</li>
    </ul>
  </li>

  <li>
    <strong>📊 MS Avaliador de Crédito (<code>msavaliadorcredito</code>)</strong><br />
    Orquestrador que consome os outros serviços via <strong>OpenFeign</strong> e publica mensagens no <strong>RabbitMQ</strong>.
    <br /><em>Fluxos:</em>
    <ul>
      <li><strong>Situação do Cliente:</strong> Agrega dados de <em>MS Clientes</em> e <em>MS Cartões</em>.</li>
      <li><strong>Avaliação:</strong> Calcula cartões aprovados baseado na renda e idade.</li>
      <li><strong>Emissão:</strong> Publica mensagem na fila <code>emissao-cartoes</code>.</li>
    </ul>
  </li>
</ul>

<h2>🛠️ Stack Tecnológico</h2>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11-orange" alt="Java 11" />
  <img src="https://img.shields.io/badge/Spring%20Boot-2.6.x-green" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring%20Cloud-2021.0.1-blue" alt="Spring Cloud" />
  <img src="https://img.shields.io/badge/RabbitMQ-Mensageria-orange" alt="RabbitMQ" />
  <img src="https://img.shields.io/badge/Docker-Container-blue" alt="Docker" />
</p>

<h2>⚙️ Configuração de Mensageria</h2>

<p>O sistema depende da seguinte fila no RabbitMQ, definida no arquivo <code>MQConfig.java</code>:</p>

<pre><code>queue: emissao-cartoes</code></pre>

<h2>▶️ Como Executar</h2>

<ol>
  <li>
    <strong>Subir infraestrutura (RabbitMQ):</strong>
    <pre><code>docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.9-management</code></pre>
  </li>
  <li>Inicie o <strong>Eureka Server</strong> (Porta 8761).</li>
  <li>Inicie o <strong>Cloud Gateway</strong> (Porta 8080).</li>
  <li>Inicie os microsserviços: <em>msclientes</em>, <em>mscartoes</em> e <em>msavaliadorcredito</em>.</li>
</ol>
