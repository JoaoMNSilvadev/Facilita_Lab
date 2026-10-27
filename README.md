# FacilitaLab

Sistema web de gestão de workflow odontológico, desenvolvido para um laboratório de prótese dentária e seus dentistas clientes. O FacilitaLab substitui o processo manual de pedidos em papel, WhatsApp e arquivos enviados por e-mail ou mídia física por uma plataforma centralizada, com fluxo controlado e histórico completo de cada pedido.

> Projeto acadêmico desenvolvido no SENAI Camaçari (SEDE), BA.

---

## Sobre o projeto

Laboratórios odontológicos ainda enfrentam dificuldades para organizar pedidos digitais, principalmente no fluxo que envolve arquivos STL, comunicação entre setores e acompanhamento do andamento. Processos manuais ou descentralizados aumentam erros, retrabalho e atrasos na entrega.

O FacilitaLab permite:

- gerenciar digitalmente pedidos de próteses;
- acompanhar o ciclo de vida de cada pedido por meio de uma **máquina de estados**;
- versionar arquivos de design (STL e modelagem);
- centralizar a comunicação entre dentista, recepção e cadista;
- rastrear todas as ações realizadas no sistema.

## Perfis de usuário

| Perfil | Responsabilidades |
|---|---|
| **Dentista** | Criar pedidos, anexar arquivos STL, acompanhar o status, aprovar ou rejeitar o design e comentar. |
| **Recepção** | Receber pedidos, fazer a triagem, aprovar ou devolver com motivo e gerenciar a fila inicial. |
| **Cadista** | Analisar pedidos tecnicamente, criar o design, gerar versões, solicitar correções e enviar para aprovação. |
| **Gestor / Dono do laboratório** | Monitorar o fluxo de pedidos, a produtividade e os prazos. |

## Fluxo do processo

1. O dentista faz login, cria um novo pedido, preenche a ficha de especificações e envia para a recepção.
2. A recepção dá entrada no pedido e o encaminha ao cadista.
3. O cadista analisa o arquivo:
   - se houver erro, ele reporta, a recepção informa o dentista e o dentista complementa as informações;
   - se estiver tudo certo, ele inicia e conclui o design.
4. O dentista avalia o design:
   - se rejeitar, o pedido volta para correção;
   - se aprovar, o pedido segue para usinagem (produção).
5. O cadista finaliza o trabalho e a recepção dá baixa no pedido.

## Funcionalidades

- **Autenticação e controle de acesso:** login por e-mail e senha com JWT, controle por perfil (RBAC), recuperação de senha e logout.
- **Gestão de pedidos:** ficha digital com arquivo STL, cor do dente, material, tipo de prótese e observações, com validação dos campos obrigatórios.
- **Triagem:** a recepção aprova ou devolve o pedido, e a devolução exige motivo.
- **Upload e versionamento:** arquivos STL de até 100 MB, com todas as versões preservadas e download de qualquer versão.
- **Workflow por estados:** transições controladas por estado atual e perfil do ator, com bloqueio de transições não autorizadas.
- **Comunicação por pedido:** comentários internos com data, hora e autor, além de aprovação ou pedido de revisão do design pelo dentista.
- **Rastreabilidade e auditoria:** log imutável de todas as ações e linha do tempo completa do pedido.
- **Painel gerencial:** visão dos pedidos por estado, produtividade por cadista e filtros por data, estado, dentista ou cadista.

## Regras de negócio principais

- Um pedido só avança se o estado atual permitir a transição e o ator tiver o perfil autorizado.
- A devolução na triagem exige motivo obrigatório.
- O design só vai para aprovação com pelo menos um arquivo de design vinculado à versão atual.
- A rejeição do design cria automaticamente uma nova versão (v+1) no estado `EM_CORRECAO`.
- Um pedido só pode ser `FINALIZADO` depois de passar por `EM_PRODUCAO`.
- O dentista só visualiza e interage com os próprios pedidos.
- O estado `AGUARDANDO_INFORMACOES` só pode ser ativado pela recepção durante a triagem.
- O número de versão do design é sequencial e gerado pelo sistema.
- Registros de auditoria e comentários são imutáveis.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Front-end | React |
| Back-end | Java 21 + Spring Boot 4.0.8 (Spring Security, Spring Data JPA) |
| Banco de dados | MySQL |
| Autenticação | JWT |
| Armazenamento de STL | Serviço de nuvem externo (ex.: AWS S3) |

## Pré-requisitos

- [Java 21](https://adoptium.net/)
- [MySQL](https://dev.mysql.com/downloads/) rodando em `localhost:3306`
- Git

## Como executar

1. Clone o repositório:

```bash
git clone <URL_DO_REPOSITORIO>
cd facilitalab
```

2. Configure o banco de dados em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/facilitalab_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
spring.datasource.username=SEU_USUARIO_MYSQL
spring.datasource.password=SUA_SENHA_MYSQL
```

O banco `facilitalab_db` é criado automaticamente se não existir.

3. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe por padrão em `http://localhost:8080`.

## Testes

Para rodar os testes automatizados:

```bash
./mvnw test
```

## Requisitos não funcionais

- Senhas armazenadas com hash bcrypt.
- JWT com expiração de no máximo 8 horas.
- Todas as rotas exigem autenticação, exceto login e recuperação de senha.
- Isolamento de dados entre dentistas.
- Arquitetura em camadas, com separação de responsabilidades.
- Compatibilidade com Chrome, Firefox e Edge.

## Escopo do MVP

Fora do escopo desta versão: aplicativo mobile nativo, integração com fresadoras e impressoras 3D, emissão de notas fiscais e gestão financeira. A produção (`EM_PRODUCAO`) é apenas um status de controle. O sistema atende a um único laboratório.

## Equipe

- Kaio Antunes
- João Matheus do Nascimento Silva
- Paulo Filipe Nascimento Santana
- Victor Hugo Reis

SENAI SEDE, Camaçari, BA.
## Pitch
https://canva.link/vt77bpmt49g06kx
