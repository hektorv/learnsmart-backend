#!/usr/bin/env python3
"""
Classroom Simulation - Load Test
Runs multiple instances of the React Learning Simulation concurrently to test system robustness.
"""
import concurrent.futures
import time
import sys
import os

# Ensure we can import the simulation script
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from simulate_react_learning import run_simulation

# Configuration
NUM_STUDENTS = 5  # Number of concurrent students
MAX_WORKERS = 5   # Concurrency limit (Thread pool size)

def run_student(student_id):
    print(f"🚀 Starting simulation for Student {student_id}...")
    try:
        run_simulation(simulation_id=student_id)
        print(f"✅ Student {student_id} FINISHED successfully.")
        return True
    except Exception as e:
        print(f"❌ Student {student_id} FAILED: {e}")
        return False

def main():
    print(f"=== CLASSROOM SIMULATION LOAD TEST ===")
    print(f"Simulating {NUM_STUDENTS} concurrent students exploring the platform.\n")
    
    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        # Generate unique IDs for each student to prevent data collision
        student_ids = [int(time.time()) + i for i in range(NUM_STUDENTS)]
        
        future_to_student = {executor.submit(run_student, sid): sid for sid in student_ids}
        
        results = []
        for future in concurrent.futures.as_completed(future_to_student):
            student_id = future_to_student[future]
            try:
                success = future.result()
                results.append(success)
            except Exception as exc:
                print(f"Student {student_id} generated an exception: {exc}")
                results.append(False)
                
    end_time = time.time()
    duration = end_time - start_time
    
    success_count = sum(1 for r in results if r)
    print(f"\n=== LOAD TEST SUMMARY ===")
    print(f"Total Students: {NUM_STUDENTS}")
    print(f"Successful:     {success_count}")
    print(f"Failed:         {len(results) - success_count}")
    print(f"Total Time:     {duration:.2f} seconds")
    
    if success_count == NUM_STUDENTS:
        print("\n✅ SYSTEM ROBUSTNESS VERIFIED: All concurrent sessions completed successfully.")
        sys.exit(0)
    else:
        print("\n❌ SYSTEM INSTABILITY DETECTED: Some sessions failed.")
        sys.exit(1)

if __name__ == "__main__":
    main()
