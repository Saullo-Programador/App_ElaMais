# Ela+ 🌸 | Seu companheiro de cuidado e ciclo menstrual

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-%234285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture-green?style=for-the-badge)
![Unit Tests](https://img.shields.io/badge/Tests-Unit_%2F_Integration_%2F_UI-blue?style=for-the-badge)

O **Ela+** é um aplicativo Android moderno focado em saúde feminina, projetado para ajudar mulheres a monitorar seu ciclo menstrual de forma inteligente, oferecendo previsões precisas, lembretes personalizados e sugestões de autocuidado baseadas em cada fase do ciclo.

---

## 🚀 Funcionalidades principais

- **Cálculo de Ciclo Inteligente:** Previsões baseadas em média histórica (Modo Avançado) ou dados fixos (Modo Simples).
- **Rastreador de Fases:** Visualização em tempo real das fases Menstrual, Folicular, Ovulatória e Luteal.
- **Notificações Inteligentes:** Lembretes de período fértil, TPM e início do ciclo com horários configuráveis.
- **Histórico Completo:** Registro de ciclos passados para maior precisão das previsões.
- **Dicas de Autocuidado:** Sugestões dinâmicas de bem-estar para cada fase hormonal.

---

## 🏗️ Arquitetura e Tech Stack

O projeto segue os princípios de **Clean Architecture** e **SOLID**, garantindo um código testável, escalável e de fácil manutenção.

- **Linguagem:** Kotlin + Coroutines & Flow (Programação Reativa).
- **UI:** Jetpack Compose (Modern Declarative UI).
- **Injeção de Dependência:** Hilt (Dagger).
- **Persistência de Dados:** Room Database (SQLite).
- **Background Tasks:** WorkManager para agendamento de notificações.
- **Arquitetura:** MVVM (Model-View-ViewModel) + UseCases para isolamento da lógica de negócio.

---

## 🧪 Estratégia de Testes (Quality Assurance)

A qualidade do projeto é garantida por uma suíte de testes robusta, seguindo a Pirâmide de Testes:

### 1. Testes de Unidade (`JUnit 4`, `MockK`)
- **Lógica de Domínio:** Validação do `GetCycleInfoUseCase` com múltiplos cenários (ciclos irregulares, datas vazias, etc).
- **ViewModels:** Teste de emissão de estados da UI e fluxos de navegação.
- **Mappers:** Garantia de integridade na conversão entre camadas (Entity <-> Domain <-> DTO).

### 2. Testes de Integração (`Room`, `AndroidTest`)
- **Persistence Layer:** Testes de DAOs em banco de dados *in-memory* para garantir a integridade dos dados salvos.

### 3. Testes de UI (`Compose Test Rule`)
- **User Flows:** Verificação visual e de interação na `HomeScreen`, garantindo que o usuário veja a informação correta em cada fase.

---

## 📦 Como executar o projeto

1. Clone o repositório:
   