# Benchmark

## Ingestion Performance Test

### Machine
- OS: Windows 11
- CPU: Intel Core Ultra 5 125H (3.60 GHz)
- RAM: 16 GB

### Test Setup
- Endpoint: POST /events/batch
- Batch size: 1000 events
- Payload file: events_1000.json
- Database: MySQL (local)
- Tool used: Postman

### Result
- Measured response time: **699 ms**
- Requirement met: **Yes (under 1 second)**

### Notes
- Test executed on a local machine
- Events were validated, deduplicated, and stored according to business rules
- Multiple defect values (0, positive, -1) were included in the dataset
- Deduplication logic was verified by re-sending the same batch