# Week 2 UML Documentation

## Alert Generation System

The Alert Generation System is designed around classes that each have a clear job. `AlertGenerator` is responsible for checking patient measurements against medical rules. `PatientThreshold` stores the safe minimum and maximum values for a metric, so threshold rules are not hidden inside one large method. `Alert` represents the warning that is created when a value is unsafe. `AlertManager` receives alerts, keeps track of active ones, and decides when they are resolved. `MedicalStaffNotifier` is separate because sending a message to staff is different from deciding whether an alert exists. `DataStorage` and `PatientData` show where the generator gets the patient information from.

This separation makes the design easier to understand and maintain. For example, if the hospital later wants email, SMS, or dashboard notifications, only the notifier behavior needs to change. If new medical limits are added, the thresholds can be updated without rewriting the whole alert workflow. Safety is supported by checking values before creating alerts and by giving every alert a severity, message, patient identifier, and timestamp. This helps staff understand what happened and when. Privacy is considered by passing only the data needed for alerting, such as patient ID, metric name, and value, instead of exposing full patient records. The design also supports data flow clearly: storage provides patient data, the generator evaluates it, and the manager handles the created alert.

## Data Storage System

The Data Storage System focuses on keeping patient measurements organized, controlled, and traceable. `DataStorage` is the central class because it saves patient data, retrieves records, and applies rules before data is returned. `PatientData` represents one measurement, such as oxygen saturation or blood pressure. `Patient` owns many patient data records, which shows the connection between a person and their stored measurements. `DataRetriever` is included to separate search and query behavior from the storage class, keeping the main storage class simpler.

The supporting classes show safety and privacy responsibilities. `AccessController` checks whether a user role is allowed to read or write records. This prevents every part of the system from automatically seeing sensitive patient data. `AuditLog` records access and changes, which is important in a healthcare system because patient information should be traceable. `DeletionPolicy` supports privacy and maintainability by describing when old data should be removed. This prevents the design from keeping unnecessary data forever.

The design is extensible because storage, retrieval, access control, auditing, and deletion are separate concerns. A future version could replace the storage with a database, add stronger permissions, or change retention rules without redesigning the whole subsystem. Correctness is supported by centralizing data access through `DataStorage` and `DataRetriever`, so other systems do not need to know the internal list structure.

## Patient Identification System

The Patient Identification System exists to connect incoming simulator or hospital data to the correct patient identity. `PatientIdentifier` is the main entry point. It receives `IncomingPatientData` and asks `IdentityManager` to find the correct `HospitalPatient`. This keeps the public identification workflow simple while allowing the matching rules to grow later. `HospitalPatient` stores the hospital-safe identity, including a hospital patient ID and safe display name. `PatientRegistry` stores known patients and provides lookup behavior.

Responsibilities are separated so the design stays understandable. `IncomingPatientData` represents raw data from outside the identity system. `IdentityManager` handles matching decisions, while `PatientRegistry` only stores and finds registered patients. `IdentityMismatchException` is included because patient identification is safety-critical. If incoming data does not match confidently, the system should not silently attach medical readings to the wrong patient. Instead, it can raise an error and ask for review.

This subsystem supports privacy by using identifiers instead of exposing unnecessary personal details. The `safeDisplayName` attribute suggests that screens can show a limited name rather than full sensitive information. Correctness is the main safety goal: data must be linked to the right patient before it moves to storage or alert generation. The design is extensible because new matching rules, extra hospital identifiers, or temporary patient identities can be added inside `IdentityManager` without changing every other subsystem.

## Data Access Layer

The Data Access Layer is responsible for collecting data from different input sources and converting it into a common format for the rest of the CHMS. `DataListener` is an interface that defines common methods for starting, stopping, and receiving data. `TCPDataListener`, `WebSocketDataListener`, and `FileDataListener` implement that interface for different source types. This keeps source-specific logic separate and makes it easy to add another listener later, such as a database or API listener.

`DataParser` is separate from the listeners because receiving raw text is not the same responsibility as checking and converting it. The parser validates the raw data and creates `PatientData`. `DataSourceAdapter` connects a listener to the parser. It acts as a simple bridge between external data sources and the internal patient data format. This design shows data flow clearly: a listener receives raw data, the adapter passes it to the parser, and the parser creates structured patient data.

Safety and correctness are supported by validating raw data before it becomes part of the system. Invalid or badly formatted data can be rejected by `DataParser` instead of being stored or used for alerts. Maintainability is improved because each listener can be tested independently. Privacy is supported by converting external input into the limited `PatientData` structure, so later subsystems receive only the fields they need.
