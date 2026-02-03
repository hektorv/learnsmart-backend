# 🔢 Gap Prioritization Matrix (Epic 9)

**Total Gaps:** 42  
**Date:** February 3, 2026

## 🚨 Critical Priority (Immediate Action)

| US ID | Title | Category | Impact | Effort | Justification |
|-------|-------|----------|--------|--------|---------------|
| **US-090** | Email Confirmation | Security | High | Medium | Security best practice, prevents fake accounts. |
| **US-107** | Auto Replanning Triggers | Automation | High | Medium | Reduces manual effort, increases adaptivity value. |
| **US-113** | Session Management | UX/Logic | High | Medium | Prevents session hijacking, allows resuming work. |
| **US-114** | IRT/CAT Algorithm | Core AI | High | Large | Scientific validity of the assessment platform. |

## 🔥 High Priority (Next Sprint)

| US ID | Title | Category | Impact | Effort | Justification |
|-------|-------|----------|--------|--------|---------------|
| **US-091** | Password Complexity | Security | High | Low | Easy win for security hardening. |
| **US-111** | Skill Prereq Validation | Logic | High | Medium | Prevents generating impossible learning plans. |
| **US-112** | Diagnostic Level Detection | Core AI | High | Large | Improves initial plan personalization (Cold Start). |
| **US-123** | Payload Validation | Data | High | Low | Prevents data corruption in analytics. |
| **US-124** | Role-Based Access (Tracking) | Security | High | Low | Privacy compliance requirement. |
| **US-130** | AI Performance Monitoring | DevOps | High | Medium | Critical for SLA enforcement and cost control. |
| **US-109** | Progress Calculation | UX | Med | Medium | Key for user motivation and clarity. |
| **US-110** | Completion Timestamps | Analytics | High | Low | Foundational data point for all time-based metrics. |
| **US-129** | Performance Monitoring (Tracking)| DevOps | High | Medium | ensures system stability under load. |
| **US-094** | User Audit Trail | Security | High | Medium | Accountability and security auditing. |

## ⚠️ Medium Priority (Short Term)

| US ID | Title | Category | Impact | Effort | Justification |
|-------|-------|----------|--------|--------|---------------|
| **US-108** | Replan Approval Workflow | UX | Med | Medium | Gives user control but adds friction; optional initially. |
| **US-116** | Session Progress Indicators | UX | Med | Low | Good UX but not blocking functionality. |
| **US-117** | On-Demand Feedback | UX/AI | Med | Low | Nice to have, standard feedback is already implemented. |
| **US-093** | Skill Validation (Catalog) | Logic | Med | Medium | Data integrity, but catalog changes rarely. |
| **US-099** | CONTENT_CREATOR Role | Security | Med | Medium | Admin can handle content initially. |
| **US-081** | Explicit Prereqs API | Technical | Med | Low | Admin feature, workaround exists via DB/generic update. |
| **US-125** | Cursor Pagination | Performance | Med | Medium | Offset works for small datasets (MVP). |
| **US-126** | Event Export | Analytics | Med | Medium | Analysts can query DB directly in interim. |
| **US-127** | Event Aggregations | Analytics | Med | Medium | Can be done client-side or via SQL for MVP. |
| **US-131** | Follow-Up Feedback | UX/AI | Med | Medium | Advanced feature, conversational depth. |
| **US-115** | Item Deduplication | Logic | Med | Low | Improves UX, avoids repetition irritation. |
| **US-096** | Goal Completion Tracking | Logic | Med | Medium | Important but depends on progress tracking. |
| **US-118** | Mastery Trend Analysis | Analytics | Med | Large | Valuable insight but complex to visualize/calculate. |

## 🧊 Low Priority (Backlog)

| US ID | Title | Category | Impact | Effort | Justification |
|-------|-------|----------|--------|--------|---------------|
| **US-092** | Course Statistics | Analytics | Low | Medium | Nice to have summary stats. |
| **US-095** | Physical Deletion of Goals | Logic | Low | Low | Soft delete is safer; physical delete is edge case. |
| **US-097** | Consistent HTTP Status | Technica l| Low | Low | API polish, doesn't break functionality. |
| **US-098** | Performance Optimization | Technical | Low | Large | Premature optimization before load testing. |
| **US-100** | Content Versioning | Logic | Low | Large | Complex feature, not needed for MVP content static nature. |
| **US-101** | Skill-Based Content Filter | UX | Low | Low | Nice search feature. |
| **US-102** | User Completion Integration | Logic | Low | Medium | Already tracked via events, direct integration is sugar. |
| **US-103** | Pedagogical Ordering | Logic | Low | Large | Manual ordering works for now. |
| **US-104** | Domain Enrichment | UX | Low | Low | Visual candy (icons/colors). |
| **US-105** | Difficulty Filtering | UX | Low | Low | Admin feature. |
| **US-106** | Proper HTTP 409 | Technical | Low | Low | Error handling polish. |
| **US-119** | Low Mastery Highlights | UX | Low | Low | Derived from mastery data. |
| **US-120** | Peer Comparison | Analytics | Low | Large | Advanced gamification/social feature. |
| **US-121** | Subtopic Breakdown | Analytics | Low | Large | Requires content tagging maturity first. |
| **US-122** | Positive Reinforcement | UX | Low | Low | Text copy enhancement. |
| **US-128** | Query Result Limits | Performance | Low | Low | Good practice, easy to implement later. |

---

## 📅 Suggested Phasing

### Phase 1: Security & Stability (Next 2 Sprints)
**Focus:** Critical + High Security/DevOps items.
- US-090, US-091, US-123, US-124, US-129, US-130, US-094

### Phase 2: Core Enhancements (Sprints 3-5)
**Focus:** Critical Automation + High Logic/AI items.
- US-107, US-113, US-114, US-111, US-112, US-110, US-109

### Phase 3: Analyst & User Experience (Sprints 6-8)
**Focus:** Medium UX/Analytics items.
- US-108, US-116, US-117, US-125, US-126, US-127, US-118

### Phase 4: Polish & Advanced Features (Sprint 9+)
**Focus:** Low priority items and complex enhancements.
- Remaining items.
