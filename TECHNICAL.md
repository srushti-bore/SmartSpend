# SmartSpend Technical Documentation

## Architecture Overview
- **Pattern:** Clean Architecture / Layered MVVM (Presentation $\rightarrow$ Domain $\rightarrow$ Data $\rightarrow$ Local Encrypted Storage).
- **Language:** Kotlin 1.9.22
- **UI:** Jetpack Compose with Material 3 (single-activity architecture, `FLAG_SECURE` window protection)
- **Local Database:** Room 2.6.1 + SQLCipher 4.6.0 (AES-256 encrypted at rest, Schema v2)
- **Background Jobs:** AndroidX WorkManager 2.9.0 + Hilt Worker (`RecurringExpenseWorker`)
- **App Widgets:** Jetpack Glance 1.0.0 (`SmartSpendGlanceWidget`)
- **Key Management:** Android Keystore (`KeystoreManager` with AES-GCM 256)
- **Dependency Injection:** Hilt 2.50
- **Money & Math:** `BigDecimal` with `RoundingMode.HALF_EVEN`
- **Preferences:** Jetpack DataStore (`PreferencesManager`)

---

## Directory Structure
```
app/src/main/java/com/smartspend/app/
├── SmartSpendApplication.kt
├── MainActivity.kt
├── core/
│   ├── common/             # Resource<T>, DispatcherProvider
│   ├── database/           # SmartSpendDatabase (v2), Converters
│   ├── datastore/          # PreferencesManager
│   ├── money/              # MoneyUtils (BigDecimal formatting & math)
│   ├── notification/       # NotificationHelper (Budget warnings, Recurring alerts)
│   ├── security/           # KeystoreManager, HashUtils
│   ├── ui/                 # Theme, Typography, Components (Cards, States)
│   └── worker/             # RecurringExpenseWorker (WorkManager)
├── data/
│   ├── local/              # Room Entities (Profile, Category, PaymentMethod, Expense, Budget, Income, Account, RecurringExpense, SavingsGoal, SavingsGoalContribution) & DAOs
│   ├── mapper/             # Entity <-> Domain mappers (Mappers.kt)
│   └── repository/         # Repository implementations with profileId scoping
├── domain/
│   ├── model/              # Domain models (Profile, Expense, Category, PaymentMethod, Budget, Income, CashFlowSummary, Account, RecurringExpense, SavingsGoal)
│   ├── repository/         # Domain repository interfaces
│   └── usecase/            # Pure Kotlin Use Cases (Profile, Expense, Category, Budget, Income, Account, Recurring, SavingsGoal, Export)
├── feature/
│   ├── onboarding/         # OnboardingScreen & ViewModel
│   ├── profile/            # LockScreen & ViewModel
│   ├── dashboard/          # DashboardScreen & ViewModel (Net Cash Flow Bento Card)
│   ├── expense/            # AddEditExpense & Ledger Screens / ViewModels
│   ├── budget/             # BudgetScreen & ViewModel
│   ├── category/           # CategoryScreen & ViewModel
│   ├── income/             # IncomeScreen & ViewModel (Income CRUD & Cashflow)
│   ├── account/            # AccountScreen & ViewModel (Wallets & Balances)
│   ├── recurring/          # RecurringExpenseScreen & ViewModel (Subscriptions)
│   ├── savingsgoal/        # SavingsGoalScreen & ViewModel (Milestones & Deposits)
│   ├── export/             # ExportDialog (CSV / Share Sheet)
│   └── widget/             # SmartSpendGlanceWidget & Receiver
├── di/                     # Hilt Modules (DatabaseModule, RepositoryModule, SecurityModule)
└── navigation/             # Screen routes & SmartSpendNavGraph
```

---

## Security & Data Rules
1. **Zero Cross-Profile Leakage:** All DAO queries filter by `profileId` at the SQL query level.
2. **Referential Integrity:** Categories/Payment Methods with active recorded transactions cannot be deleted without prior reassignment.
3. **No Fake Data:** All metrics, cash flows, and charts derive purely from genuine user transactions.
4. **Validation Rules:** Strictly positive amounts, non-empty titles, rejection of future-dated transactions, target date future enforcement for goals.
5. **Exact Arithmetic:** All monetary computations use `BigDecimal` with half-even rounding; zero Float/Double monetary representations.
