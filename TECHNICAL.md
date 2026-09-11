# SmartSpend Technical Documentation

## Architecture Overview
- **Pattern:** Clean Architecture / Layered MVVM (Presentation $\rightarrow$ Domain $\rightarrow$ Data $\rightarrow$ Local Encrypted Storage).
- **Language:** Kotlin 1.9.22
- **UI:** Jetpack Compose with Material 3 (single-activity architecture, `FLAG_SECURE` window protection)
- **Local Database:** Room 2.6.1 + SQLCipher 4.6.0 (AES-256 encrypted at rest, Schema v2)
- **Background Jobs:** AndroidX WorkManager 2.9.0 + Hilt Worker (`RecurringExpenseWorker`)
- **Vision & OCR:** CameraX 1.3.1 + Google ML Kit Text Recognition 16.0.0 (100% on-device offline OCR)
- **App Widgets:** Jetpack Glance 1.0.0 (`SmartSpendGlanceWidget`)
- **Key Management:** Android Keystore (`KeystoreManager` with AES-GCM 256)
- **Dependency Injection:** Hilt 2.50
- **Money & Math:** `BigDecimal` with `RoundingMode.HALF_EVEN`
- **Preferences:** Jetpack DataStore (`PreferencesManager`)
- **Image Loading:** Coil 2.5.0

---

## Directory Structure
```
app/src/main/java/com/smartspend/app/
├── SmartSpendApplication.kt
├── MainActivity.kt         # Handlers for FLAG_SECURE & ACTION_SEND image shares
├── core/
│   ├── common/             # Resource<T>, DispatcherProvider
│   ├── database/           # SmartSpendDatabase (v2), Converters
│   ├── datastore/          # PreferencesManager
│   ├── money/              # MoneyUtils (BigDecimal formatting & math)
│   ├── notification/       # NotificationHelper (Budget warnings, Recurring alerts)
│   ├── security/           # KeystoreManager, HashUtils
│   ├── ui/                 # Theme, Typography, Components (Cards, States, DuplicateWarningBanner, VisualMoodMascot)
│   └── worker/             # RecurringExpenseWorker (WorkManager)
├── data/
│   ├── local/              # Room Entities & DAOs (Profile, Category, PaymentMethod, Expense, Budget, Income, Account, RecurringExpense, SavingsGoal, SavingsGoalContribution)
│   ├── mapper/             # Entity <-> Domain mappers (Mappers.kt)
│   └── repository/         # Repository implementations with profileId scoping
├── domain/
│   ├── assisted/           # Assisted Capture Engines (NaturalLanguageParser, ReceiptOcrParser, SmartCategorySuggester, ParsedExpenseDraft)
│   ├── intelligence/       # Financial Intelligence Engines:
│   │   ├── SafeToSpendEngine.kt            # Safe daily/weekly run-rate calculator
│   │   ├── FinancialHealthScoreUseCase.kt  # 5-pillar 0–100 score + recommendation engine
│   │   ├── LeakHunterUseCase.kt            # Micro-spend & subscription leak detector
│   │   ├── PurchaseSimulatorUseCase.kt     # "Can I afford this?" scenario simulator
│   │   ├── SpendForecasterUseCase.kt       # Linear burn rate & 50/30/20 ratio analyzer
│   │   ├── AskSmartSpendEngine.kt          # Grounded multilingual AI chat engine (मराठी, हिंदी, English)
│   │   └── StreakManager.kt                # Habit streak & badge calculator
│   ├── model/              # Domain models (Profile, Expense, Category, PaymentMethod, Budget, Income, CashFlowSummary, Account, RecurringExpense, SavingsGoal)
│   ├── repository/         # Domain repository interfaces
│   └── usecase/            # Pure Kotlin Use Cases:
│       ├── backup/         # EncryptedBackupUseCase & EncryptedRestoreUseCase
│       ├── split/          # SplitExpenseUseCase (Equal, percentage, exact splits)
│       └── ...             # Core profile, expense, income, budget, account, recurring use cases
├── feature/
│   ├── onboarding/         # OnboardingScreen & ViewModel
│   ├── profile/            # LockScreen & ViewModel
│   ├── dashboard/          # DashboardScreen & ViewModel (Net Cash Flow, Brainy Mascot, Streaks, Fast Capture)
│   ├── expense/            # AddEditExpense & Ledger Screens / ViewModels
│   ├── budget/             # BudgetScreen & ViewModel
│   ├── category/           # CategoryScreen & ViewModel
│   ├── income/             # IncomeScreen & ViewModel
│   ├── account/            # AccountScreen & ViewModel
│   ├── recurring/          # RecurringExpenseScreen & ViewModel
│   ├── savingsgoal/        # SavingsGoalScreen & ViewModel
│   ├── intelligence/       # IntelligenceHubScreen & AskSmartSpendScreen + ViewModels
│   ├── backup/             # BackupRestoreScreen & ViewModel
│   ├── report/             # ReportsScreen & ViewModel (Visual trends & breakdowns)
│   ├── split/              # SplitExpenseScreen & ViewModel (Split bill & WhatsApp sharing)
│   ├── export/             # ExportDialog (CSV / Share Sheet)
│   ├── assisted/
│   │   ├── quickadd/       # QuickAddBottomSheet & QuickAddViewModel (Natural Language)
│   │   ├── voice/          # VoiceExpenseBottomSheet (SpeechRecognizer integration)
│   │   ├── ocr/            # ReceiptScannerScreen & ReceiptScannerViewModel (CameraX + ML Kit)
│   │   └── importcsv/      # CsvImportScreen & CsvImportViewModel (Batch CSV import)
│   └── widget/             # SmartSpendGlanceWidget & Receiver
├── di/                     # Hilt Modules (DatabaseModule, RepositoryModule, SecurityModule)
└── navigation/             # Screen routes & SmartSpendNavGraph
```

---

## Financial Intelligence & AI Grounding Invariants
1. **Zero Hallucinations:** Ask SmartSpend engine executes deterministic SQL queries on the user's local Room database and applies mathematical formulas (Safe-to-Spend, 50/30/20 ratio, Health Score). No generative models invent financial numbers.
2. **Deficit-Tolerant Accounting:** Pacing run-rates, safe daily allowances, and simulation results gracefully handle negative cash flows and budget overruns with explicit advice and warnings.
3. **Hardware Keystore AES-256 Backups:** `.smartspend` export bundles are encrypted with AES-GCM-256 using key material guarded by the Android Keystore.
4. **Cent-Exact Split Math:** Bill splitting uses integer cent remainders assigned to the payer, ensuring `sum(participants) == totalAmount` with zero rounding loss.
5. **IST Timezone Grace Buffer:** Habit streaks operate in `Asia/Kolkata` with a 24-hour grace window to preserve streaks when transactions are recorded the following morning.
