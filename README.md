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

## 🖼️ Visualização

| Home Screen | Ciclo | Cuidados | Lembrete | Configurações |
| :---: | :---: | :---: | :---: | :---: |
| <img width="200" height="457" alt="Homee" src="https://github.com/user-attachments/assets/6cea0014-d00a-4cc3-bc71-0e86e617a290" /> | <img width="200" height="457" alt="Ciclo" src="https://github.com/user-attachments/assets/1c9c6bca-3cbf-4689-8fac-368b544fce81" /> | <img width="200" height="457" alt="Cuidado" src="https://github.com/user-attachments/assets/01d57587-74e8-42e1-8c25-f1b490bd32f3" /> | <img width="200" height="457" alt="Lembrete" src="https://github.com/user-attachments/assets/815da481-67fa-4f94-910c-caf5e669bb3b" /> | <img width="200" height="457" alt="configurações" src="https://github.com/user-attachments/assets/8044d913-ee18-4e40-aa9f-fec486903e2e" /> |



---

## 🏗️ Arquitetura e Tech Stack

O projeto segue os princípios de **Clean Architecture** e **SOLID**, garantindo um código testável, escalável e de fácil manutenção.

- **Linguagem:** Kotlin + Coroutines & Flow (Programação Reativa).
- **UI:** Jetpack Compose (Modern Declarative UI).
- **Injeção de Dependência:** Hilt (Dagger).
- **Persistência de Dados:** Room Database (SQLite).
- **Background Tasks:** WorkManager para agendamento de notificações.
- **Cloud/Auth:** Firebase (Auth, Firestore, Messaging).
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

### Pré-requisitos
- **Android Studio** (Versão Ladybug ou superior recomendada).
- **JDK 17**.
- **Android SDK 36**.
- Dispositivo Android físico ou Emulador (API 24+).

### Passo a Passo
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/App_ElaMais.git
   ```
2. Abra o projeto no **Android Studio**.
3. Aguarde a sincronização do **Gradle** finalizar.
4. Se estiver usando Firebase, adicione seu arquivo `google-services.json` na pasta `app/`.
5. Execute o app no seu dispositivo ou emulador.

---

## 🤝 Contribuição

Contribuições são muito bem-vindas! Para contribuir:

1. Faça um **Fork** do projeto.
2. Crie uma **Branch** para sua feature (`git checkout -b feature/NovaFeature`).
3. Faça o **Commit** de suas alterações (`git commit -m 'feat: Adicionando nova funcionalidade'`).
4. Faça o **Push** para a branch (`git push origin feature/NovaFeature`).
5. Abra um **Pull Request**.

---

## 📄 Licença

Este projeto está sob a licença **MIT**. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👤 Autor

Desenvolvido por **Saullo Programador** - [Seu LinkedIn](https://linkedin.com/in/seu-perfil) | [Seu GitHub](https://github.com/seu-usuario)
